/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.discovery.MySensorsDiscoveryService;
import org.openhab.binding.mysensors.internal.Pair;
import org.openhab.binding.mysensors.internal.event.MySensorsEventObserver_OLD;
import org.openhab.binding.mysensors.internal.event.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.exception.NoMoreIdsException;
import org.openhab.binding.mysensors.internal.handler.MySensorsBridgeHandler;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsDeviceManager;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection of the bridge (via TCP/IP or serial) to the MySensors network.
 *
 * @author Tim Oberföll
 * @author Andrea Cioni
 *
 */
public abstract class MySensorsBridgeConnection implements Runnable, MySensorsUpdateListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    // Connector will check for connection status every CONNECTOR_INTERVAL_CHECK seconds
    public static final int CONNECTOR_INTERVAL_CHECK = 10;

    // ??
    private boolean pauseWriter = false;

    // Device manager
    private MySensorsDeviceManager deviceManager;

    // Blocking queue wait for message
    private BlockingQueue<MySensorsMessage> outboundMessageQueue = null;

    // Flag setted to true while connection is up
    private boolean connected = false;

    // Flag to be set (through available method below)
    private boolean requestDisconnection = false;

    private MySensorsBridgeConnection waitingObj = null;

    // I_VERSION response flag
    private boolean iVersionResponse = false;

    // Check connection on startup flag
    private boolean skipStartupCheck = false;

    // Reader and writer thread
    protected MySensorsWriter mysConWriter = null;
    protected MySensorsReader mysConReader = null;

    // Bridge handler dependency
    private MySensorsBridgeHandler bridgeHandler = null;

    // Sanity checker
    private MySensorsNetworkSanityChecker netSanityChecker = null;

    // Connection retry done
    private int numOfRetry = 0;

    // Connection status watchdog
    private ScheduledExecutorService watchdogExecutor = null;
    private Future<?> futureWatchdog = null;

    public MySensorsBridgeConnection(MySensorsDeviceManager deviceManager, MySensorsBridgeHandler bridgeHandler) {
        this.deviceManager = deviceManager;
        this.outboundMessageQueue = new LinkedBlockingQueue<MySensorsMessage>();
        this.bridgeHandler = bridgeHandler;
        this.watchdogExecutor = Executors.newSingleThreadScheduledExecutor();
        this.iVersionResponse = false;
    }

    /**
     * Initialization of the BridgeConnection
     */
    public void initialize() {
        logger.debug("Set skip check on startup to: {}", bridgeHandler.getBridgeConfiguration().skipStartupCheck);
        skipStartupCheck = bridgeHandler.getBridgeConfiguration().skipStartupCheck;

        // Launch connection watchdog
        logger.debug("Enabling connection watchdog");
        futureWatchdog = watchdogExecutor.scheduleWithFixedDelay(this, 0, CONNECTOR_INTERVAL_CHECK, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        Thread.currentThread().setName(MySensorsBridgeConnection.class.getName());

        if (requestingDisconnection()) {
            logger.info("Connection request disconnection...");
            requestDisconnection(false);
            disconnect();
        }

        if (!connected) {
            if (connect()) {
                logger.info("Successfully connected to MySensors Bridge.");

                numOfRetry = 0;

                // Start discovery service
                MySensorsDiscoveryService discoveryService = new MySensorsDiscoveryService(bridgeHandler);
                discoveryService.activate();

                if (bridgeHandler.getBridgeConfiguration().enableNetworkSanCheck) {

                    // Start network sanity check
                    netSanityChecker = new MySensorsNetworkSanityChecker(this);
                    netSanityChecker.start();

                } else {
                    logger.warn("Network Sanity Checker thread disabled from bridge configuration");
                }

            } else {
                logger.error("Failed connecting to bridge...next retry in {} seconds (Retry No.:{})",
                        CONNECTOR_INTERVAL_CHECK, numOfRetry);
                numOfRetry++;
                disconnect();
            }

        } else {
            logger.debug("Bridge is connected, connection skipped");
        }

    }

    /**
     * Startup connection with bridge
     *
     * @return true, if connection established correctly
     */
    private boolean connect() {
        connected = _connect();
        MySensorsEventObserver_OLD.notifyBridgeStatusUpdate(this, isConnected());
        return connected;
    }

    protected abstract boolean _connect();

    /**
     * Shutdown method that allows the correct disconnection with the used bridge
     */
    private void disconnect() {

        if (netSanityChecker != null) {
            netSanityChecker.stop();
            netSanityChecker = null;
        }

        _disconnect();
        connected = false;
        requestDisconnection = false;
        iVersionResponse = false;

        MySensorsEventObserver_OLD.notifyBridgeStatusUpdate(this, isConnected());
    }

    protected abstract void _disconnect();

    /**
     * Stop all threads holding the connection (serial/tcp).
     */
    public void destroy() {
        logger.debug("Destroying connection");

        if (connected) {
            disconnect();
        }

        if (futureWatchdog != null) {
            futureWatchdog.cancel(true);
            futureWatchdog = null;
        }

        if (watchdogExecutor != null) {
            watchdogExecutor.shutdown();
            watchdogExecutor.shutdownNow();
        }

        MySensorsEventObserver_OLD.clearAllListeners();
    }

    /**
     * Start thread managing the incoming/outgoing messages. It also have the task to test the connection to gateway by
     * sending a special message (I_VERSION) to it
     *
     * @return true if the gateway test pass successfully
     */
    protected boolean startReaderWriterThread(MySensorsReader reader, MySensorsWriter writer) {

        reader.startReader();
        writer.startWriter();

        MySensorsEventObserver_OLD.addEventListener(this);

        if (!skipStartupCheck) {
            try {
                int i = 0;
                synchronized (this) {
                    while (!iVersionResponse && i < 5) {
                        addMySensorsOutboundMessage(MySensorsBindingConstants.I_VERSION_MESSAGE);
                        waitingObj = this;
                        waitingObj.wait(1000);
                        i++;
                    }
                }
            } catch (Exception e) {
                logger.error("Exception on waiting for I_VERSION message", e);
            }
        } else {
            logger.warn("Skipping I_VERSION connection test, not recommended...");
            iVersionResponse = true;
        }

        if (!iVersionResponse) {
            logger.error(
                    "Cannot start reading/writing thread, probably sync message (I_VERSION) not received. Try set skipStartupCheck to true");
        }

        return iVersionResponse;
    }

    /**
     * Add a message to the outbound queue. The message will be send automatically. FIFO queue.
     *
     * @param msg The message that should be send.
     */
    public void addMySensorsOutboundMessage(MySensorsMessage msg) {
        addMySensorsOutboundMessage(msg, 1);
    }

    /**
     * Store more than one message in the outbound queue.
     *
     * @param msg the message that should be stored in the queue.
     * @param copy the number of copies that should be stored.
     */
    private void addMySensorsOutboundMessage(MySensorsMessage msg, int copy) {
        synchronized (outboundMessageQueue) {
            try {
                for (int i = 0; i < copy; i++) {
                    outboundMessageQueue.put(msg);
                }
            } catch (InterruptedException e) {
                logger.error("Interrupted message while ruuning");
            }
        }

    }

    /**
     * Get the next message in line from the queue.
     *
     * @return the next message in line.
     * @throws InterruptedException
     */
    public MySensorsMessage pollMySensorsOutboundQueue() throws InterruptedException {
        return outboundMessageQueue.poll(1, TimeUnit.DAYS);
    }

    /**
     * Remove a message from the outbound message queue.
     *
     * @param msg The message that should be removed from the queue.
     */
    public void removeMySensorsOutboundMessage(MySensorsMessage msg) {

        pauseWriter = true;

        Iterator<MySensorsMessage> iterator = outboundMessageQueue.iterator();
        if (iterator != null) {
            while (iterator.hasNext()) {
                MySensorsMessage msgInQueue = iterator.next();
                // logger.debug("Msg in Queue: " + msgInQueue.getDebugInfo());
                if (msgInQueue.getNodeId() == msg.getNodeId() && msgInQueue.getChildId() == msg.getChildId()
                        && msgInQueue.getMsgType() == msg.getMsgType() && msgInQueue.getSubType() == msg.getSubType()
                        && msgInQueue.getAck() == msg.getAck() && msgInQueue.getMsg().equals(msg.getMsg())) {
                    iterator.remove();
                    // logger.debug("Message removed: " + msg.getDebugInfo());
                } else {
                    logger.debug("Message NOT removed: " + msg.getDebugInfo());
                }
            }
        }

        pauseWriter = false;
    }

    /**
     * Status for the writer / message sender.
     *
     * @return true if writer is paused.
     */
    public boolean isWriterPaused() {
        return pauseWriter;
    }

    /**
     * Is a connection to the bridge available?
     *
     * @return true, if connection is up and running.
     */
    public boolean isConnected() {
        return connected;
    }

    public boolean requestingDisconnection() {
        return requestDisconnection;
    }

    /**
     * Start the disconnection process.
     *
     * @param flag true if the connection should be stopped.
     */
    public void requestDisconnection(boolean flag) {
        logger.debug("Request disconnection flag setted to: " + flag);
        requestDisconnection = flag;
    }

    @Override
    public void messageReceived(MySensorsMessage message) throws Throwable {
        if (!handleIncomingMessage(message)) {
            // If the message was not handled previously try to handle it as a special one
            handleSpecialMessageEvent(message);
        }
    }

    /**
     * Wake up main thread that is waiting for confirmation of link up
     */
    private void handleIncomingVersionMessage(String message) {
        iVersionMessageReceived(message);
    }

    private void iVersionMessageReceived(String msg) {
        if (waitingObj != null) {
            logger.debug("Good,Gateway is up and running! (Ver:{})", msg);
            synchronized (waitingObj) {
                iVersionResponse = true;
                waitingObj.notifyAll();
                waitingObj = null;
            }
        }
    }

    private void handleSpecialMessageEvent(MySensorsMessage msg) {
        // Do we get an ACK?
        if (msg.getAck() == 1) {
            logger.debug(String.format("ACK received! Node: %d, Child: %d", msg.nodeId, msg.childId));
            removeMySensorsOutboundMessage(msg);
        }

        // Have we get a I_CONFIG message?
        if (msg.isIConfigMessage()) {
            answerIConfigMessage(msg);
        }

        // Have we get a I_TIME message?
        if (msg.isITimeMessage()) {
            answerITimeMessage(msg);
        }

        // Have we get a I_VERSION message?
        if (msg.isIVersionMessage()) {
            handleIncomingVersionMessage(msg.msg);
        }

        // Requesting ID
        if (msg.isIdRequestMessage()) {
            answerIDRequest();
        }
    }

    /**
     * If an ID-Request from a sensor is received the controller will send an id to the sensor
     */
    private void answerIDRequest() {
        logger.info("ID Request received");

        int newId = 0;
        try {
            newId = deviceManager.reserveId();
            logger.info("New Node in the MySensors network has requested an ID. ID is: {}", newId);
            MySensorsMessage newMsg = new MySensorsMessage(255, 255, 3, 0, false, 4, newId + "");
            addMySensorsOutboundMessage(newMsg);
            MySensorsEventObserver_OLD.notifyNodeIdReserved(newId);
        } catch (NoMoreIdsException e) {
            logger.error("No more IDs available for this node, try cleaning cache");
        }
    }

    /**
     * Handle the incoming message from serial
     *
     * @param msg the incoming message
     * @return true if ,and only if, the message is propagated to one of the defined node or message arrives from a
     *         device new device in the network
     * @throws Throwable
     */
    private boolean handleIncomingMessage(MySensorsMessage msg) throws Throwable {
        boolean ret = false;
        if (MySensorsNode.isValidNodeId(msg.nodeId) && MySensorsChild.isValidChildId(msg.childId)
                && msg.isSetReqMessage()) {
            MySensorsNode node = deviceManager.getNode(msg.nodeId);
            if (node != null) {
                logger.debug("Node {} found in device manager", msg.nodeId);

                node.setLastUpdate(new Date());

                MySensorsChild child = node.getChild(msg.childId);
                if (child != null) {
                    logger.debug("Child {} found in node {}", msg.childId, msg.nodeId);

                    child.setLastUpdate(new Date());

                    MySensorsVariable variable = child.getVariable(msg.msgType, msg.subType);
                    if (variable != null) {
                        variable.setValue(msg);
                        MySensorsEventObserver_OLD.notifyNodeUpdateEvent(node, child, variable);
                        ret = true;
                    } else {
                        logger.warn("Variable {}({}) not present", msg.subType,
                                CHANNEL_MAP.get(new Pair<Integer>(msg.msgType, msg.subType)));
                    }
                } else {
                    logger.debug("Child {} not present into node {}", msg.childId, msg.nodeId);
                }
            } else {
                logger.debug("Node {} not present, send new node discovered event", msg.nodeId);

                node = new MySensorsNode(msg.nodeId);
                deviceManager.addNode(node);
                MySensorsEventObserver_OLD.notifyNewNodeDiscovered(node);
                ret = true;
            }
        }

        return ret;
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

        logger.debug("I_CONFIG request received from {}, answering: (is imperial?){}", iConfig, imperial);

        MySensorsMessage newMsg = new MySensorsMessage(msg.nodeId, msg.childId, MYSENSORS_MSG_TYPE_INTERNAL, 0, false,
                MYSENSORS_SUBTYPE_I_CONFIG, iConfig);
        addMySensorsOutboundMessage(newMsg);

    }

}
