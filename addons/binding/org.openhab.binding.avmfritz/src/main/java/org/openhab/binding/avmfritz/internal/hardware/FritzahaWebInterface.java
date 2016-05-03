/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.hardware;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.avmfritz.config.AvmFritzConfiguration;
import org.openhab.binding.avmfritz.handler.IFritzHandler;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles requests to a Fritz!OS web interface for interfacing with
 * AVM home automation devices. It manages authentication and wraps commands.
 * 
 * @author Robert Bausdorf, Christian Brauers 
 * 
 */
public class FritzahaWebInterface {

	/**
	 * Configuration of the bridge from {@link org.openhab.BoxHandler.fritzaha.handler.FritzAhaBridgeHandler}
	 */
	protected AvmFritzConfiguration config;
	/**
	 * Current session ID
	 */
	protected String sid;
	/**
	 * HTTP client for asynchronous calls
	 */
	protected HttpClient asyncclient;
	/**
	 * Maximum number of simultaneous asynchronous connections
	 */
	protected int asyncmaxconns = 20;
	/**
	 * Bridge thing handler for updating thing status
	 */
	protected IFritzHandler fbHandler;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	// Uses RegEx to handle bad FritzBox XML
	/**
	 * RegEx Pattern to grab the session ID from a login XML response
	 */
	protected static final Pattern SID_PATTERN = Pattern
			.compile("<SID>([a-fA-F0-9]*)</SID>");
	/**
	 * RegEx Pattern to grab the challenge from a login XML response
	 */
	protected static final Pattern CHALLENGE_PATTERN = Pattern
			.compile("<Challenge>(\\w*)</Challenge>");
	/**
	 * RegEx Pattern to grab the access privilege for home automation functions
	 * from a login XML response
	 */
	protected static final Pattern ACCESS_PATTERN = Pattern
			.compile("<Name>HomeAuto</Name>\\s*?<Access>([0-9])</Access>");

	/**
	 * This method authenticates with the Fritz!OS Web Interface and updates the
	 * session ID accordingly
	 * 
	 * @return New session ID
	 */
	public String authenticate() {
		if (this.config.getPassword() == null) {
			this.fbHandler.setStatusInfo(
					ThingStatus.OFFLINE, 
					ThingStatusDetail.CONFIGURATION_ERROR, 
					"please configure password first");
			return null;
		}
		String loginXml = HttpUtil.executeUrl("GET",
				getURL("login_sid.lua", addSID("")),
				10 * this.config.getSyncTimeout());
		if (loginXml == null) {
			this.fbHandler.setStatusInfo(
					ThingStatus.OFFLINE, 
					ThingStatusDetail.COMMUNICATION_ERROR, 
					"FRITZ!Box does not respond");
			return null;
		}
		Matcher sidmatch = SID_PATTERN.matcher(loginXml);
		if (!sidmatch.find()) {
			this.fbHandler.setStatusInfo(
					ThingStatus.OFFLINE, 
					ThingStatusDetail.COMMUNICATION_ERROR, 
					"FRITZ!Box does not respond with SID");
			return null;
		}
		sid = sidmatch.group(1);
		Matcher accmatch = ACCESS_PATTERN.matcher(loginXml);
		if (accmatch.find()) {
			if (accmatch.group(1) == "2") {
				this.fbHandler.setStatusInfo(
						ThingStatus.ONLINE, 
						ThingStatusDetail.NONE, 
						"Resuming FRITZ!Box connection with SID " + sid);
				return sid;
			}
		}
		Matcher challengematch = CHALLENGE_PATTERN.matcher(loginXml);
		if (!challengematch.find()) {
			this.fbHandler.setStatusInfo(
					ThingStatus.OFFLINE, 
					ThingStatusDetail.COMMUNICATION_ERROR, 
					"FRITZ!Box does not respond with challenge for authentication");
			return null;
		}
		String challenge = challengematch.group(1);
		String response = createResponse(challenge);
		loginXml = HttpUtil.executeUrl(
				"GET",
				getURL("login_sid.lua",
						(this.config.getUser() != null && !"".equals(this.config.getUser()) 
							? ("username=" + this.config.getUser() + "&") : "")
							+ "response=" + response), 
						this.config.getSyncTimeout());
		if (loginXml == null) {
			this.fbHandler.setStatusInfo(
					ThingStatus.OFFLINE, 
					ThingStatusDetail.COMMUNICATION_ERROR, 
					"FRITZ!Box does not respond");
			return null;
		}
		sidmatch = SID_PATTERN.matcher(loginXml);
		if (!sidmatch.find()) {
			this.fbHandler.setStatusInfo(
					ThingStatus.OFFLINE, 
					ThingStatusDetail.COMMUNICATION_ERROR, 
					"Resuming FRITZ!Box connection with SID");
			return null;
		}
		sid = sidmatch.group(1);
		accmatch = ACCESS_PATTERN.matcher(loginXml);
		if (accmatch.find()) {
			if (accmatch.group(1) == "2") {
				this.fbHandler.setStatusInfo(
						ThingStatus.ONLINE, 
						ThingStatusDetail.NONE, 
						"Established FRITZ!Box connection with SID " + sid);
				return sid;
			}
		}
		this.fbHandler.setStatusInfo(
				ThingStatus.OFFLINE, 
				ThingStatusDetail.CONFIGURATION_ERROR, 
				"User " + this.config.getUser() + " has no access to FritzBox home automation functions");
		return null;
	}

	/**
	 * Checks the authentication status of the web interface
	 * 
	 * @return
	 */
	public boolean isAuthenticated() {
		return !(sid == null);
	}

	public AvmFritzConfiguration getConfig() {
		return config;
	}

	public void setConfig(AvmFritzConfiguration config) {
		this.config = config;
	}

	/**
	 * Creates the proper response to a given challenge based on the password
	 * stored
	 * 
	 * @param challenge
	 *            Challenge string as returned by the Fritz!OS login script
	 * @return Response to the challenge
	 */
	protected String createResponse(String challenge) {
		String handshake = challenge.concat("-").concat(
				this.config.getPassword());
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			logger.error("This version of Java does not support MD5 hashing");
			return "";
		}
		byte[] handshakeHash;
		try {
			handshakeHash = md5.digest(handshake.getBytes("UTF-16LE"));
		} catch (UnsupportedEncodingException e) {
			logger.error("This version of Java does not understand UTF-16LE encoding");
			return "";
		}
		String response = challenge.concat("-");
		for (byte handshakeByte : handshakeHash)
			response = response.concat(String.format("%02x", handshakeByte));
		return response;
	}

	/**
	 * Constructor to set up interface
	 * @param config Bridge configuration
	 */
	public FritzahaWebInterface(AvmFritzConfiguration config, IFritzHandler handler) {
		this.config = config;
		this.fbHandler = handler;
		sid = null;
		asyncclient = new HttpClient(new SslContextFactory(true));
		asyncclient.setMaxConnectionsPerDestination(asyncmaxconns);
		try {
			asyncclient.start();
		} catch (Exception e) {
			logger.error("Could not start HTTP Client for " + getURL(""));
		}
		authenticate();
		logger.debug("Starting with SID " + sid);
	}

	/**
	 * Constructs a URL from the stored information and a specified path
	 * 
	 * @param path
	 *            Path to include in URL
	 * @return URL
	 */
	public String getURL(String path) {
		return this.config.getProtocol()
				+ "://"
				+ this.config.getIpAddress()
				+ (this.config.getPort() != null ? ":" + this.config.getPort() : "")
				+ "/" + path;
	}

	/**
	 * Constructs a URL from the stored information, a specified path and a
	 * specified argument string
	 * 
	 * @param path
	 *            Path to include in URL
	 * @param args
	 *            String of arguments, in standard HTTP format
	 *            (arg1=value1&arg2=value2&...)
	 * @return URL
	 */
	public String getURL(String path, String args) {
		return getURL(path + "?" + args);
	}

	public String addSID(String args) {
		if (sid == null)
			return args;
		else
			return ("".equals(args) ? ("sid=") : (args + "&sid=")) + sid;
	}

	/**
	 * Sends an HTTP GET request using the asynchronous client
	 * 
	 * @param Path
	 *            Path of the requested resource
	 * @param Args
	 *            Arguments for the request
	 * @param Callback
	 *            Callback to handle the response with
	 */
	public FritzahaContentExchange asyncGet(String path, String args,
			FritzAhaCallback callback) {
		if (!isAuthenticated())
			authenticate();
		FritzahaContentExchange getExchange = new FritzahaContentExchange(
				callback);
		asyncclient.newRequest(this.getURL(path, this.addSID(args)))
				.method(HttpMethod.GET).onResponseSuccess(getExchange)
				.onResponseFailure(getExchange) //.onComplete(getExchange)
				.send(getExchange);
		logger.debug("GETting URL " + getURL(path, addSID(args)));
		return getExchange;
	}

	public FritzahaContentExchange asyncGet(FritzAhaCallback callback) {
		return this.asyncGet(callback.getPath(), callback.getArgs(), callback);
	}

	/**
	 * Sends an HTTP POST request using the asynchronous client
	 * 
	 * @param Path
	 *            Path of the requested resource
	 * @param Args
	 *            Arguments for the request
	 * @param Callback
	 *            Callback to handle the response with
	 */
	public FritzahaContentExchange asyncPost(String path, String args,
			FritzAhaCallback callback) {
		if (!isAuthenticated())
			authenticate();

		FritzahaContentExchange postExchange = new FritzahaContentExchange(
				callback);

		this.asyncclient.newRequest(this.getURL(path))
				.timeout(this.config.getAsyncTimeout(), TimeUnit.SECONDS)
				.method(HttpMethod.POST).onResponseSuccess(postExchange)
				.onResponseFailure(postExchange) //.onComplete(postExchange)
				.content(new StringContentProvider(this.addSID(args), "UTF-8"))
				.send(postExchange);
		return postExchange;
	}
}
