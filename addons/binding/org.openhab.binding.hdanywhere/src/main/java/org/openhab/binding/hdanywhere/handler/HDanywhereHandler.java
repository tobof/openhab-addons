/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdanywhere.handler;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.hdanywhere.HDanywhereBindingConstants.Port;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HDanywhereHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 */
public class HDanywhereHandler extends BaseThingHandler {

    // List of Configurations constants
    public static final String IP_ADDRESS = "ipAddress";
    public static final String PORTS = "ports";
    public static final String POLLING_INTERVAL = "interval";

    private Logger logger = LoggerFactory.getLogger(HDanywhereHandler.class);

    private ScheduledFuture<?> pollingJob;

    /**
     * the timeout to use for connecting to a given host (defaults to 5000
     * milliseconds)
     */
    private static int timeout = 5000;

    public HDanywhereHandler(Thing thing) {
        super(thing);
    }

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                String host = (String) getConfig().get(IP_ADDRESS);
                int numberOfPorts = ((BigDecimal) getConfig().get(PORTS)).intValue();

                String httpMethod = "GET";
                String url = "http://" + host + "/status_show.shtml";

                if (isNotBlank(httpMethod) && isNotBlank(url)) {
                    String response = HttpUtil.executeUrl(httpMethod, url, null, null, null, timeout);

                    if (response != null) {
                        updateStatus(ThingStatus.ONLINE);

                        for (int i = 1; i <= numberOfPorts; i++) {
                            Pattern p = Pattern.compile("var out" + i + "var = (.*);");
                            Matcher m = p.matcher(response);

                            while (m.find()) {
                                DecimalType decimalType = new DecimalType(m.group(1));
                                updateState(new ChannelUID(getThing().getUID(), Port.get(i).channelID()), decimalType);
                            }
                        }
                    } else {
                        updateStatus(ThingStatus.OFFLINE);
                    }
                }
            } catch (Exception e) {
                logger.warn("An exception occurred while polling the HDanwywhere matrix: '{}'", e.getMessage());
            }
        }
    };

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        String channelID = channelUID.getId();

        String host = (String) getConfig().get(IP_ADDRESS);
        int numberOfPorts = ((BigDecimal) getConfig().get(PORTS)).intValue();
        int sourcePort = Integer.valueOf(command.toString());
        int outputPort = Port.get(channelID).toNumber();

        if (sourcePort > numberOfPorts) {
            // nice try - we can switch to a port that does not physically exist
            logger.warn("Source port {} goes beyond the physical number of {} ports available on the matrix {}",
                    new Object[] { sourcePort, numberOfPorts, host });
        } else if (outputPort > numberOfPorts) {
            // nice try - we can switch to a port that does not physically exist
            logger.warn("Output port {} goes beyond the physical number of {} ports available on the matrix {}",
                    new Object[] { outputPort, numberOfPorts, host });
        } else {

            String httpMethod = "GET";
            String url = "http://" + host + "/switch.cgi?command=3&data0=";

            url = url + String.valueOf(outputPort) + "&data1=";
            url = url + command.toString() + "&checksum=";

            int checksum = 3 + outputPort + sourcePort;
            url = url + String.valueOf(checksum);

            HttpUtil.executeUrl(httpMethod, url, null, null, null, timeout);
        }

    }

    @Override
    public void dispose() {
        logger.debug("Disposing HDanywhere matrix handler.");
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing HDanywhere matrix handler.");
        onUpdate();

        updateStatus(ThingStatus.OFFLINE);
    }

    private synchronized void onUpdate() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            int polling_interval = ((BigDecimal) getConfig().get(POLLING_INTERVAL)).intValue();
            pollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, 1, polling_interval, TimeUnit.SECONDS);
        }
    }
}
