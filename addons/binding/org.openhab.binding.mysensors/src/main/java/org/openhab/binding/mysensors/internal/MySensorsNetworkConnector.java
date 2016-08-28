package org.openhab.binding.mysensors.internal;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.MySensorsBindingUtility;
import org.openhab.binding.mysensors.discovery.MySensorsDiscoveryService;
import org.openhab.binding.mysensors.handler.MySensorsBridgeHandler;
import org.openhab.binding.mysensors.handler.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.handler.MySensorsUpdateListener;
import org.openhab.binding.mysensors.protocol.ip.MySensorsIpConnection;
import org.openhab.binding.mysensors.protocol.serial.MySensorsSerialConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySensorsNetworkConnector implements Runnable, MySensorsUpdateListener {

    // Logger
    private Logger logger = LoggerFactory.getLogger(getClass());

    // Bridge handler dependency
    private MySensorsBridgeHandler bridgeHandler = null;

    // Connection
    private MySensorsBridgeConnection myCon = null;

    // Sanity checker
    private MySensorsNetworkSanityChecker netSanityChecker = null;

    // Connection retry done
    private int numOfRetry = 0;

    public MySensorsNetworkConnector(MySensorsBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    public void run() {

        if (myCon == null) {
            if (bridgeHandler.getThing().getThingTypeUID().equals(THING_TYPE_BRIDGE_SER)) {
                myCon = new MySensorsSerialConnection(bridgeHandler.getBridgeConfiguration().serialPort,
                        bridgeHandler.getBridgeConfiguration().baudRate,
                        bridgeHandler.getBridgeConfiguration().sendDelay,
                        bridgeHandler.getBridgeConfiguration().skipStartupCheck);
            } else if (bridgeHandler.getThing().getThingTypeUID().equals(THING_TYPE_BRIDGE_ETH)) {
                myCon = new MySensorsIpConnection(bridgeHandler.getBridgeConfiguration().ipAddress,
                        bridgeHandler.getBridgeConfiguration().tcpPort,
                        bridgeHandler.getBridgeConfiguration().sendDelay,
                        bridgeHandler.getBridgeConfiguration().skipStartupCheck);
            }
        }

        if (!myCon.isConnected()) {
            if (myCon.connect()) {
                logger.info("Successfully connected to MySensors Bridge.");

                numOfRetry = 0;

                bridgeHandler.notifyConnect();

                // Start discovery service
                MySensorsDiscoveryService discoveryService = new MySensorsDiscoveryService(bridgeHandler);
                discoveryService.activate();

                if (bridgeHandler.getBridgeConfiguration().enableNetworkSanCheck) {
                    logger.info("Network Sanity Checker thread started");

                    // Start network sanity check
                    netSanityChecker = new MySensorsNetworkSanityChecker();
                    netSanityChecker.start();

                } else {
                    logger.warn("Network Sanity Checker thread disabled from bridge configuration");
                }

            } else {
                logger.error("Failed connecting to bridge...next retry in {} seconds (Retry No.:{})", 10, numOfRetry);
                numOfRetry++;
                // removeUpdateListener(bridgeHandler);
            }

        } else {
            logger.debug("Bridge is connected, connection skipped");
        }
    }

    public void addMySensorsOutboundMessage(MySensorsMessage msg, int copy) {
        myCon.addMySensorsOutboundMessage(msg, copy);
    }

    public void addMySensorsOutboundMessage(MySensorsMessage msg) {
        myCon.addMySensorsOutboundMessage(msg);
    }

    public MySensorsMessage pollMySensorsOutboundQueue() throws InterruptedException {
        return myCon.pollMySensorsOutboundQueue();
    }

    public void removeMySensorsOutboundMessage(MySensorsMessage msg) {
        myCon.removeMySensorsOutboundMessage(msg);
    }

    public void addUpdateListener(MySensorsUpdateListener listener) {
        myCon.addUpdateListener(listener);
    }

    public void removeUpdateListener(MySensorsUpdateListener listener) {
        myCon.removeUpdateListener(listener);
    }

    public boolean isWriterPaused() {
        return myCon.isWriterPaused();
    }

    public boolean isConnected() {
        return myCon.isConnected();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.mysensors.handler.MySensorsUpdateListener#statusUpdateReceived(org.openhab.binding.mysensors.
     * handler.MySensorsStatusUpdateEvent)
     */
    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {
        MySensorsMessage msg = event.getData();
        // logger.debug("updateRecieved: " + msg.getDebugInfo());
        // Do we get an ACK?
        if (msg.getAck() == 1) {
            logger.debug(String.format("ACK received! Node: %d, Child: %d", msg.nodeId, msg.childId));
            removeMySensorsOutboundMessage(msg);
        }

        // Are we getting a Request ID Message?
        if (MySensorsBindingUtility.isIdRequestMessage(msg)) {
            answerIDRequest();
        }

        // Have we get a I_VERSION message?
        if (MySensorsBindingUtility.isIVersionMessage(msg)) {
            handleIncomingVersionMessage(msg.msg);
        }

        // Have we get a I_CONFIG message?
        if (MySensorsBindingUtility.isIConfigMessage(msg)) {
            answerIConfigMessage(msg);
        }

        // Have we get a I_TIME message?
        if (MySensorsBindingUtility.isITimeMessage(msg)) {
            answerITimeMessage(msg);
        }
    }

    @Override
    public void disconnectEvent() {

    }

    public void disconnect() {

        if (netSanityChecker != null) {
            netSanityChecker.stop();
            netSanityChecker = null;
        }

        if (myCon != null) {
            myCon.disconnect();
            myCon = null;
        }
    }

    /**
     * Answer to I_TIME message for gateway time request from sensor
     *
     * @param msg, the incoming I_TIME message from sensor
     */
    private void answerITimeMessage(MySensorsMessage msg) {
        logger.info("I_TIME request received from {}, answering...", msg.nodeId);

        String time = Long.toString(System.currentTimeMillis() / 1000);
        MySensorsMessage newMsg = new MySensorsMessage(msg.nodeId, msg.childId, MYSENSORS_MSG_TYPE_INTERNAL, 0, false,
                MYSENSORS_SUBTYPE_I_TIME, time);
        addMySensorsOutboundMessage(newMsg);

    }

    /**
     * Answer to I_CONFIG message for imperial/metric request from sensor
     *
     * @param msg, the incoming I_CONFIG message from sensor
     */
    private void answerIConfigMessage(MySensorsMessage msg) {
        boolean imperial = bridgeHandler.getBridgeConfiguration().imperial;
        String iConfig = imperial ? "I" : "M";

        logger.debug("I_CONFIG request received from {}, answering: {}", iConfig);

        MySensorsMessage newMsg = new MySensorsMessage(msg.nodeId, msg.childId, MYSENSORS_MSG_TYPE_INTERNAL, 0, false,
                MYSENSORS_SUBTYPE_I_CONFIG, iConfig);
        addMySensorsOutboundMessage(newMsg);

    }

    /**
     * If an ID -Request from a sensor is received the controller will send an id to the sensor
     */
    private void answerIDRequest() {
        logger.debug("ID Request received");

        int newId = bridgeHandler.getFreeId();
        bridgeHandler.getGivedIds().add(newId);
        MySensorsMessage newMsg = new MySensorsMessage(255, 255, 3, 0, false, 4, newId + "");
        addMySensorsOutboundMessage(newMsg);
        logger.info("New Node in the MySensors network has requested an ID. ID is: {}", newId);
    }

    /**
     * Wake up main thread that is waiting for confirmation of link up
     */
    private void handleIncomingVersionMessage(String message) {
        myCon.iVersionMessageReceived(message);
    }

    private class MySensorsNetworkSanityChecker implements MySensorsUpdateListener, Runnable {

        private Logger logger = LoggerFactory.getLogger(getClass());

        private static final int SHEDULE_MINUTES_DELAY = 1; // only for test will be: 3
        private static final int MAX_ATTEMPTS_BEFORE_DISCONNECT = 1; // only for test will be: 3

        private ScheduledFuture<?> future = null;

        private Integer iVersionMessageMissing = 0;
        private boolean iVersionMessageArrived = false;

        public void start() {

            if (bridgeHandler.getBridgeConfiguration().enableNetworkSanCheck) {
                logger.info("Network Sanity Checker thread started");

                iVersionMessageArrived = false;
                iVersionMessageMissing = 0;

                future = bridgeHandler.getScheduler().scheduleWithFixedDelay(MySensorsNetworkSanityChecker.this,
                        SHEDULE_MINUTES_DELAY, SHEDULE_MINUTES_DELAY, TimeUnit.MINUTES);

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

        }

        @Override
        public void run() {
            try {
                addUpdateListener(this);

                addMySensorsOutboundMessage(MySensorsBindingConstants.I_VERSION_MESSAGE);

                Thread.sleep(3000);

                synchronized (iVersionMessageMissing) {
                    if (!iVersionMessageArrived) {
                        logger.warn(
                                "I_VERSION message response is not arrived. Remained attempts before disconnection {}",
                                MAX_ATTEMPTS_BEFORE_DISCONNECT - iVersionMessageMissing);

                        if ((MAX_ATTEMPTS_BEFORE_DISCONNECT - iVersionMessageMissing) <= 0) {
                            logger.error("Retry period expired, gateway is down. Disconneting bridge...");

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
                removeUpdateListener(this);
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

}
