/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.MySensorsBindingUtility;
import org.openhab.binding.mysensors.config.MySensorsBridgeConfiguration;
import org.openhab.binding.mysensors.handler.MySensorsBridgeHandler;
import org.openhab.binding.mysensors.handler.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.handler.MySensorsUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Cioni
 *
 *         Network sanity checker for bridge connection
 */

public class MySensorsNetworkSanityChecker implements MySensorsUpdateListener, Runnable {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final int SHEDULE_MINUTES_DELAY = 1; // only for test will be: 3
    private static final int MAX_ATTEMPTS_BEFORE_DISCONNECT = 1; // only for test will be: 3

    private MySensorsBridgeHandler myBridgeHandler = null;
    private MySensorsBridgeConnection myBridgeConnection = null;
    private MySensorsBridgeConfiguration myConfiguration = null;

    private ScheduledExecutorService executor = null;
    private ScheduledFuture<?> future = null;

    private Integer iVersionMessageMissing = 0;
    private boolean iVersionMessageArrived = false;

    public MySensorsNetworkSanityChecker(MySensorsBridgeHandler myBridgeHandler,
            MySensorsBridgeConfiguration myConfiguration) {

        if (myBridgeConnection == null || myConfiguration == null) {
            throw new IllegalArgumentException();
        }
        this.myBridgeHandler = myBridgeHandler;
        this.myBridgeConnection = myBridgeHandler.getBridgeConnection();
        this.myConfiguration = myConfiguration;
    }

    public void start() {

        if (myConfiguration.enableNetworkSanCheck) {
            logger.info("Network Sanity Checker thread started");

            iVersionMessageArrived = false;
            iVersionMessageMissing = 0;

            if (executor == null) {
                executor = Executors.newSingleThreadScheduledExecutor();
            }
            future = executor.scheduleAtFixedRate(this, SHEDULE_MINUTES_DELAY, SHEDULE_MINUTES_DELAY, TimeUnit.MINUTES);

        } else {
            logger.warn("Network Sanity Checker thread disabled from bridge configuration");
        }

    }

    public void stop() {
        logger.info("Network Sanity Checker thread stopped");

        if (future != null) {
            future.cancel(true);
            future = null;
        }

        if (executor != null) {
            executor.shutdown();
            executor.shutdownNow();
            executor = null;
        }

    }

    @Override
    public void run() {
        try {
            myBridgeConnection.addUpdateListener(this);

            myBridgeConnection.addMySensorsOutboundMessage(MySensorsBindingConstants.I_VERSION_MESSAGE);

            Thread.sleep(3000);

            synchronized (iVersionMessageMissing) {
                if (!iVersionMessageArrived) {
                    logger.warn("I_VERSION message response is not arrived. Remained attempts before disconnection {}",
                            MAX_ATTEMPTS_BEFORE_DISCONNECT - iVersionMessageMissing);

                    if ((MAX_ATTEMPTS_BEFORE_DISCONNECT - iVersionMessageMissing) <= 0) {
                        logger.error("Retry period expired, gateway is down. Disconneting bridge...");

                        myBridgeConnection.broadCastDisconnect();
                    } else {
                        iVersionMessageMissing++;
                    }
                } else {
                    logger.debug("Network sanity check: PASSED");
                    iVersionMessageMissing = 0;
                }

                iVersionMessageArrived = false;
            }

        } catch (InterruptedException e) {
            logger.error("interrupted exception in network sanity thread checker");
        } finally {
            myBridgeConnection.removeUpdateListener(this);
        }
    }

    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {
        synchronized (iVersionMessageMissing) {
            if (!iVersionMessageArrived) {
                iVersionMessageArrived = MySensorsBindingUtility.isIVersionMessage(event.getData());
            }
        }
    }

    @Override
    public void disconnectEvent() {
    }
}
