package org.openhab.binding.mysensors.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.MySensorsBindingUtility;
import org.openhab.binding.mysensors.handler.MySensorsBridgeHandler;
import org.openhab.binding.mysensors.handler.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.handler.MySensorsUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySensorsNetworkSanityChecker implements MySensorsUpdateListener, Runnable {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final int SHEDULE_MINUTES_DELAY = 1; // only for test will be: 3
    private static final int MAX_ATTEMPTS_BEFORE_DISCONNECT = 1; // only for test will be: 3

    private MySensorsBridgeHandler bridgeHandler = null;

    private ScheduledFuture<?> future = null;

    private Integer iVersionMessageMissing = 0;
    private boolean iVersionMessageArrived = false;

    private ScheduledExecutorService scheduler = null;

    public MySensorsNetworkSanityChecker(MySensorsBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    public void start() {

        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
        } else {
            logger.warn("Scheduler is not null");
        }

        if (bridgeHandler.getBridgeConfiguration().enableNetworkSanCheck) {
            logger.info("Network Sanity Checker thread started");

            iVersionMessageArrived = false;
            iVersionMessageMissing = 0;

            future = scheduler.scheduleWithFixedDelay(MySensorsNetworkSanityChecker.this, SHEDULE_MINUTES_DELAY,
                    SHEDULE_MINUTES_DELAY, TimeUnit.MINUTES);

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
            bridgeHandler.getBridgeConnector().addUpdateListener(this);

            bridgeHandler.getBridgeConnector().addMySensorsOutboundMessage(MySensorsBindingConstants.I_VERSION_MESSAGE);

            Thread.sleep(3000);

            synchronized (iVersionMessageMissing) {
                if (!iVersionMessageArrived) {
                    logger.warn("I_VERSION message response is not arrived. Remained attempts before disconnection {}",
                            MAX_ATTEMPTS_BEFORE_DISCONNECT - iVersionMessageMissing);

                    if ((MAX_ATTEMPTS_BEFORE_DISCONNECT - iVersionMessageMissing) <= 0) {
                        logger.error("Retry period expired, gateway is down. Disconneting bridge...");

                        bridgeHandler.getBridgeConnector().requestDisconnection(true);

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
            bridgeHandler.getBridgeConnector().removeUpdateListener(this);
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
}