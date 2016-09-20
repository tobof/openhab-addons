package org.openhab.binding.mysensors.internal;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.openhab.binding.mysensors.discovery.MySensorsDiscoveryService;
import org.openhab.binding.mysensors.handler.MySensorsBridgeHandler;
import org.openhab.binding.mysensors.handler.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.handler.MySensorsUpdateListener;
import org.openhab.binding.mysensors.protocol.ip.MySensorsIpConnection;
import org.openhab.binding.mysensors.protocol.serial.MySensorsSerialConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Cioni
 *
 *         This class check the connection status, if bridge connection is lost try to reconnect
 */
public class MySensorsNetworkConnector implements Runnable {

    // Logger
    private Logger logger = LoggerFactory.getLogger(getClass());

    // Connector will check for connection status every CONNECTOR_INTERVAL_CHECK seconds
    public static final int CONNECTOR_INTERVAL_CHECK = 10;

    // Bridge handler dependency
    private MySensorsBridgeHandler bridgeHandler = null;

    // Connection
    private MySensorsBridgeConnection myCon = null;

    // Sanity checker
    private MySensorsNetworkSanityChecker netSanityChecker = null;

    // Connection retry done
    private int numOfRetry = 0;

    // Update listener
    private List<MySensorsUpdateListener> updateListeners = null;

    // Outbound queue
    private BlockingQueue<MySensorsMessage> outboundMessageQueue = null;

    public MySensorsNetworkConnector(MySensorsBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
        this.updateListeners = new ArrayList<>();
    }

    @Override
    public void run() {
        Thread.currentThread().setName(MySensorsNetworkConnector.class.getName());

        if (checkConnection() && myCon.requestingDisconnection()) {
            logger.info("Connection request disconnection...");
            stop();
        }

        if (myCon == null) {
            if (bridgeHandler.getThing().getThingTypeUID().equals(THING_TYPE_BRIDGE_SER)) {
                myCon = new MySensorsSerialConnection(this, bridgeHandler.getBridgeConfiguration().serialPort,
                        bridgeHandler.getBridgeConfiguration().baudRate,
                        bridgeHandler.getBridgeConfiguration().sendDelay,
                        bridgeHandler.getBridgeConfiguration().skipStartupCheck);
            } else if (bridgeHandler.getThing().getThingTypeUID().equals(THING_TYPE_BRIDGE_ETH)) {
                myCon = new MySensorsIpConnection(this, bridgeHandler.getBridgeConfiguration().ipAddress,
                        bridgeHandler.getBridgeConfiguration().tcpPort,
                        bridgeHandler.getBridgeConfiguration().sendDelay,
                        bridgeHandler.getBridgeConfiguration().skipStartupCheck);
            }
        }

        if (!checkConnection()) {

            if (myCon.connect()) {
                logger.info("Successfully connected to MySensors Bridge.");

                numOfRetry = 0;

                bridgeHandler.notifyConnect();

                // Start discovery service
                MySensorsDiscoveryService discoveryService = new MySensorsDiscoveryService(bridgeHandler);
                discoveryService.activate();

                if (bridgeHandler.getBridgeConfiguration().enableNetworkSanCheck) {

                    // Start network sanity check
                    netSanityChecker = new MySensorsNetworkSanityChecker(bridgeHandler);
                    netSanityChecker.start();

                } else {
                    logger.warn("Network Sanity Checker thread disabled from bridge configuration");
                }

            } else {
                logger.error("Failed connecting to bridge...next retry in {} seconds (Retry No.:{})",
                        CONNECTOR_INTERVAL_CHECK, numOfRetry);
                numOfRetry++;
            }

        } else {
            logger.debug("Bridge is connected, connection skipped");
        }

    }

    /**
     * @param listener An Object, that wants to listen on status updates
     */
    public void addUpdateListener(MySensorsUpdateListener listener) {
        synchronized (updateListeners) {
            if (!updateListeners.contains(listener)) {
                logger.trace("Adding listener: " + listener);
                updateListeners.add(listener);
            }
        }
    }

    public void removeUpdateListener(MySensorsUpdateListener listener) {
        synchronized (updateListeners) {
            if (updateListeners.contains(listener)) {
                logger.trace("Removing listener: " + listener);
                updateListeners.remove(listener);
            }
        }
    }

    public List<MySensorsUpdateListener> getUpdateListeners() {
        return updateListeners;
    }

    public void broadCastEvent(MySensorsStatusUpdateEvent event) {
        synchronized (updateListeners) {
            for (MySensorsUpdateListener mySensorsEventListener : updateListeners) {
                logger.trace("Broadcasting event to: " + mySensorsEventListener);
                mySensorsEventListener.statusUpdateReceived(event);
            }
        }
    }

    public void addMySensorsOutboundMessage(MySensorsMessage msg) {
        myCon.addMySensorsOutboundMessage(msg);
    }

    public boolean checkConnection() {
        return myCon != null && myCon.isConnected();
    }

    public boolean requestingDisconnection() {
        return myCon.requestingDisconnection();
    }

    public void requestDisconnection(boolean flag) {
        myCon.requestDisconnection(flag);
    }

    public MySensorsBridgeHandler getBridgeHandler() {
        return bridgeHandler;
    }

    public void stop() {

        if (netSanityChecker != null) {
            netSanityChecker.stop();
            netSanityChecker = null;
        }

        if (myCon != null) {
            myCon.disconnect();
            myCon = null;
        }
    }

}
