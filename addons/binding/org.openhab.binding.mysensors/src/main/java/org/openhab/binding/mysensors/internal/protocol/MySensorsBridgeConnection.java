/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.config.MySensorsBridgeConfiguration;
import org.openhab.binding.mysensors.internal.event.MySensorsBridgeConnectionEventListener;
import org.openhab.binding.mysensors.internal.event.MySensorsEventObserver;
import org.openhab.binding.mysensors.internal.event.MySensorsEventRegister;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection of the bridge (via TCP/IP or serial) to the MySensors network.
 *
 * @author Tim Oberföll
 * @author Andrea Cioni
 *
 */
public abstract class MySensorsBridgeConnection implements Runnable,
        MySensorsEventObserver<MySensorsBridgeConnectionEventListener>, MySensorsBridgeConnectionEventListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    // Event register for MySensorsConnectionEventListener
    private MySensorsEventRegister<MySensorsBridgeConnectionEventListener> eventRegister;

    // Connector will check for connection status every CONNECTOR_INTERVAL_CHECK seconds
    public static final int CONNECTOR_INTERVAL_CHECK = 10;

    // ??
    private boolean pauseWriter = false;

    // Blocking queue wait for message
    private BlockingQueue<MySensorsMessage> outboundMessageQueue = null;

    // Flag setted to true while connection is up
    private boolean connected = false;

    // Flag to be set (through available method below)
    private boolean requestDisconnection = false;

    private MySensorsBridgeConnection waitingObj = null;

    private MySensorsBridgeConfiguration bridgeConfiguration;

    // I_VERSION response flag
    private boolean iVersionResponse = false;

    // Check connection on startup flag
    private boolean skipStartupCheck = false;

    // Reader and writer thread
    protected MySensorsWriter mysConWriter = null;
    protected MySensorsReader mysConReader = null;

    // Sanity checker
    private MySensorsNetworkSanityChecker netSanityChecker = null;

    // Connection retry done
    private int numOfRetry = 0;

    // Connection status watchdog
    private ScheduledExecutorService watchdogExecutor = null;
    private Future<?> futureWatchdog = null;

    public MySensorsBridgeConnection(MySensorsBridgeConfiguration bridgeConfiguration) {
        this.outboundMessageQueue = new LinkedBlockingQueue<MySensorsMessage>();
        this.bridgeConfiguration = bridgeConfiguration;
        this.watchdogExecutor = Executors.newSingleThreadScheduledExecutor();
        this.iVersionResponse = false;
        this.eventRegister = new MySensorsEventRegister<>();
    }

    /**
     * Initialization of the BridgeConnection
     */
    public void initialize() {
        logger.debug("Set skip check on startup to: {}", bridgeConfiguration.skipStartupCheck);
        skipStartupCheck = bridgeConfiguration.skipStartupCheck;

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
                // MySensorsDiscoveryService discoveryService = new MySensorsDiscoveryService(bridgeHandler);
                // discoveryService.activate();

                if (bridgeConfiguration.enableNetworkSanCheck) {

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
        notifyBridgeStatusUpdate(this, isConnected());
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

        notifyBridgeStatusUpdate(this, isConnected());
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

        if (!skipStartupCheck) {
            addEventListener(this);

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
            } finally {
                addEventListener(this);
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
    public void addEventListener(MySensorsBridgeConnectionEventListener listener) {
        eventRegister.addEventListener(listener);

    }

    @Override
    public void clearAllListeners() {
        eventRegister.clearAllListeners();

    }

    @Override
    public Iterator<MySensorsBridgeConnectionEventListener> getEventListenersIterator() {
        return eventRegister.getEventListenersIterator();
    }

    @Override
    public boolean isEventListenerRegisterd(MySensorsBridgeConnectionEventListener listener) {
        return eventRegister.isEventListenerRegisterd(listener);
    }

    @Override
    public void removeEventListener(MySensorsBridgeConnectionEventListener listener) {
        eventRegister.removeEventListener(listener);
    }

    @Override
    public void messageReceived(MySensorsMessage msg) throws Throwable {
        // Have we get a I_VERSION message?
        if (msg.isIVersionMessage()) {
            iVersionMessageReceived(msg.msg);
        }
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

    private void notifyBridgeStatusUpdate(MySensorsBridgeConnection connection, boolean connected) {
        Iterator<MySensorsBridgeConnectionEventListener> iterator = eventRegister.getEventListenersIterator();
        while (iterator.hasNext()) {
            MySensorsBridgeConnectionEventListener listener = iterator.next();
            logger.trace("Broadcasting event {} to: {}", connection.toString(), listener);

            try {
                listener.bridgeStatusUpdate(this, connected);
            } catch (Throwable e) {
                logger.error("Event broadcasting throw an exception", e);
            }
        }
    }

    /**
     * Notify message to listeners. <b>Should be</b> left with 'package' visibility to be available to reader/writer
     * class
     *
     * @param msg the message to send
     */
    void notifyMessageReceived(MySensorsMessage msg) {
        Iterator<MySensorsBridgeConnectionEventListener> iterator = eventRegister.getEventListenersIterator();
        while (iterator.hasNext()) {
            MySensorsBridgeConnectionEventListener listener = iterator.next();
            logger.trace("Broadcasting event {} to: {}", msg, listener);

            try {
                listener.messageReceived(msg);
            } catch (Throwable e) {
                logger.error("Event broadcasting throw an exception", e);
            }
        }

    }

}
