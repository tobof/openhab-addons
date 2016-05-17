/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.config;

import org.apache.commons.lang.builder.ToStringBuilder;
/**
 * Bean holding configuration data according to bridge.xml
 * @author Robert Bausdorf
 * 
 */
public class AvmFritzConfiguration {

	private String ipAddress;
	private Integer port;
	private String protocol;
	
	private String user;
	private String password;
	
	private Integer asyncTimeout;
	private Integer syncTimeout;
	private Integer pollingInterval;
	
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Integer getAsyncTimeout() {
		return asyncTimeout;
	}
	public void setAsyncTimeout(Integer asyncTimeout) {
		this.asyncTimeout = asyncTimeout;
	}
	public Integer getSyncTimeout() {
		return syncTimeout;
	}
	public void setSyncTimeout(Integer syncTimeout) {
		this.syncTimeout = syncTimeout;
	}
	public Integer getPollingInterval() {
		return pollingInterval;
	}
	public void setPollingInterval(Integer pollingInterval) {
		this.pollingInterval = pollingInterval;
	}
	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("IP", this.getIpAddress())
				.append("port", this.getPort())
				.append("proto", this.getProtocol())
				.append("user", this.getUser())
				.append("password", this.getPassword())
				.append("pollingInterval", this.getPollingInterval())
				.append("asyncTimeout", this.getAsyncTimeout())
				.append("syncTimeout", this.getSyncTimeout())
				.toString();
	}
}
