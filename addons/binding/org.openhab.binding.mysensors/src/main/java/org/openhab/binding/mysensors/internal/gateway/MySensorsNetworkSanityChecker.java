/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.gateway;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.mysensors.internal.event.MySensorsEventRegister;
import org.openhab.binding.mysensors.internal.event.MySensorsGatewayEventListener;
import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Regulary checks the status of the link to the gateway to the MySensors network.
 *
 * @author Andrea Cioni
 *
 */
public class MySensorsNetworkSanityChecker implements MySensorsGatewayEventListener, Runnable {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private MySensorsEventRegister myEventRegister;
    private MySensorsAbstractConnection myCon;
    private MySensorsGateway myGateway;

    private static final int SHEDULE_MINUTES_DELAY = 3; // only for test will be: 3
    private static final int MAX_ATTEMPTS_BEFORE_DISCONNECT = 3; // only for test will be: 3

    private ScheduledExecutorService scheduler = null;
    private ScheduledFuture<?> futureSanityChk = null;

    private Integer iVersionMessageMissing = 0;
    private boolean iVersionMessageArrived = false;

    public MySensorsNetworkSanityChecker(MySensorsGateway myGateway, MySensorsEventRegister myEventRegister,
            MySensorsAbstractConnection myCon) {
        this.myGateway = myGateway;
        this.myCon = myCon;
        this.myEventRegister = myEventRegister;
    }

    private void reset() {
        synchronized (iVersionMessageMissing) {
            iVersionMessageArrived = false;
            iVersionMessageMissing = 0;
        }
    }

    /**
     * Starts the sanity check of the network.
     * Tests if the connection to the bridge is still alive.
     */
    public void start() {
        reset();

        if (futureSanityChk == null && scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            futureSanityChk = scheduler.scheduleWithFixedDelay(this, SHEDULE_MINUTES_DELAY, SHEDULE_MINUTES_DELAY,
                    TimeUnit.MINUTES);
        } else {
            logger.warn("Network Sanity Checker is alredy running");
        }
    }

    /**
     * Stops the sanity check of the network.
     */
    public void stop() {
        logger.info("Network Sanity Checker thread stopped");

        if (futureSanityChk != null) {
            futureSanityChk.cancel(true);
            futureSanityChk = null;
        }

        if (scheduler != null) {
            scheduler.shutdown();
            scheduler.shutdownNow();
            scheduler = null;
        }

    }

    @Override
    public void run() {
        Thread.currentThread().setName(MySensorsNetworkSanityChecker.class.getName());

        try {

            myEventRegister.addEventListener(this);

            checkConnectionStatus();

            checkNodeStatus();

        } catch (Exception e) {
            logger.error("Exception in network sanity thread checker", e);
        } finally {
            myEventRegister.removeEventListener(this);
        }
    }

    private void checkNodeStatus() {
        // TODO
    }

    private void checkConnectionStatus() throws InterruptedException {
        myGateway.sendMessage(MySensorsMessage.I_VERSION_MESSAGE);

        Thread.sleep(3000);

        synchronized (iVersionMessageMissing) {
            if (!iVersionMessageArrived) {
                logger.warn("I_VERSION message response is not arrived. Remained attempts before disconnection {}",
                        MAX_ATTEMPTS_BEFORE_DISCONNECT - iVersionMessageMissing);

                if ((MAX_ATTEMPTS_BEFORE_DISCONNECT - iVersionMessageMissing) <= 0) {
                    logger.error("Retry period expired, gateway is down. Disconneting bridge...");

                    myCon.requestDisconnection(true);

                } else {
                    iVersionMessageMissing++;
                }
            } else {
                logger.debug("Network sanity check: PASSED");
                iVersionMessageMissing = 0;
            }

            iVersionMessageArrived = false;
        }
    }

    @Override
    public void messageReceived(MySensorsMessage message) throws Throwable {
        synchronized (iVersionMessageMissing) {
            if (!iVersionMessageArrived) {
                iVersionMessageArrived = message.isIVersionMessage();
            }
        }
    }
}
