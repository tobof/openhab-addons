/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.mysensors.internal.event.MySensorsEventRegister;
import org.openhab.binding.mysensors.internal.gateway.MySensorsGatewayConfig;
import org.openhab.binding.mysensors.internal.gateway.MySensorsNetworkSanityChecker;
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
public abstract class MySensorsAbstractConnection implements Runnable {

    // Used by the reader to request a disconnection if there are too much exception
    private static final int ERROR_COUNT_REQ_DISCONNECT = 5;

    // How often and at which times should the binding retry to send a message if requestAck is true?
    public static final int MYSENSORS_NUMBER_OF_RETRIES = 5;
    public static final int[] MYSENSORS_RETRY_TIMES = { 0, 100, 500, 1000, 2000 };

    // Wait time Arduino reset
    public final static int RESET_TIME = 3000;

    // How long should a Smartsleep message be left in the queue?
    public static final int MYSENSORS_SMARTSLEEP_TIMEOUT = 216000; // 6 hours

    protected Logger logger = LoggerFactory.getLogger(getClass());

    // Connector will check for connection status every CONNECTOR_INTERVAL_CHECK seconds
    public static final int CONNECTOR_INTERVAL_CHECK = 10;

    // ??
    private boolean pauseWriter = false;

    // Blocking queue wait for message
    private BlockingQueue<MySensorsMessage> outboundMessageQueue = null;

    // Queue for SmartSleep messages
    private Queue<MySensorsMessage> smartSleepMessageQueue = null;

    // Flag setted to true while connection is up
    private boolean connected = false;

    // Flag to be set (through available method below)
    private boolean requestDisconnection = false;

    private Object waitingObj = null;

    // I_VERSION response flag
    private boolean iVersionResponse = false;

    // Check connection on startup flag
    private boolean skipStartupCheck = false;

    // Reader and writer thread
    protected MySensorsWriter mysConWriter = null;
    protected MySensorsReader mysConReader = null;

    protected MySensorsEventRegister myEventRegister = null;

    protected MySensorsGatewayConfig myGatewayConfig = null;

    // Sanity checker
    private MySensorsNetworkSanityChecker netSanityChecker = null;

    // Connection retry done
    private int numOfRetry = 0;

    // Connection status watchdog
    private ScheduledExecutorService watchdogExecutor = null;
    private Future<?> futureWatchdog = null;

    public MySensorsAbstractConnection(MySensorsGatewayConfig myGatewayConfig, MySensorsEventRegister myEventRegister) {
        this.myEventRegister = myEventRegister;
        this.outboundMessageQueue = new LinkedBlockingQueue<MySensorsMessage>();
        this.smartSleepMessageQueue = new LinkedList<MySensorsMessage>();
        this.myGatewayConfig = myGatewayConfig;
        this.watchdogExecutor = Executors.newSingleThreadScheduledExecutor();
        this.iVersionResponse = false;
    }

    /**
     * Initialization of the BridgeConnection
     */
    public void initialize() {
        logger.debug("Set skip check on startup to: {}", myGatewayConfig.getSkipStartupCheck());
        skipStartupCheck = myGatewayConfig.getSkipStartupCheck();

        // Launch connection watchdog
        logger.debug("Enabling connection watchdog");
        futureWatchdog = watchdogExecutor.scheduleWithFixedDelay(this, 0, CONNECTOR_INTERVAL_CHECK, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        Thread.currentThread().setName(MySensorsAbstractConnection.class.getName());

        if (requestingDisconnection()) {
            logger.info("Connection request disconnection...");
            requestDisconnection(false);
            disconnect();
        }

        if (!connected) {
            if (connect()) {
                logger.info("Successfully connected to MySensors Bridge.");

                numOfRetry = 0;
            } else {
                logger.error("Failed connecting to bridge...next retry in {} seconds (Retry No.:{})",
                        CONNECTOR_INTERVAL_CHECK, numOfRetry);
                numOfRetry++;
                disconnect();
            }

        } else {
            logger.trace("Bridge is connected, connection skipped");
        }

    }

    /**
     * Startup connection with bridge
     *
     * @return true, if connection established correctly
     */
    private boolean connect() {
        connected = _connect();
        myEventRegister.notifyBridgeStatusUpdate(this, isConnected());
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

        myEventRegister.notifyBridgeStatusUpdate(this, isConnected());
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
            try {
                int i = 0;
                synchronized (this) {
                    while (!iVersionResponse && i < 5) {
                        addMySensorsOutboundMessage(MySensorsMessage.I_VERSION_MESSAGE);
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
     * This method also has the task to populate oldMessage (and keep track thought oldMsgContent map) field on
     * MySensorsMessage
     *
     * @param msg The message that should be send.
     */
    public void addMySensorsOutboundMessage(MySensorsMessage msg) {

        if (msg.isSmartSleep()) {
            addMySensorsOutboundSmartSleepMessage(msg);
        } else {
            addMySensorsOutboundMessage(msg, 1);
        }
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
     * A message to a node that supports smartsleep is not send instantly.
     * The message is send in response to a heartbeat received from this node.
     * Only one message is allowed in the queue. If a new one arrives the old one
     * gets deleted.
     *
     * @param msg the message that should be added to the queue.
     */
    private void addMySensorsOutboundSmartSleepMessage(MySensorsMessage msg) {

        // Only one pending message is allowed in the queue.
        removeSmartSleepMessage(msg.getNodeId(), msg.getChildId());

        synchronized (smartSleepMessageQueue) {
            smartSleepMessageQueue.add(msg);
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
                if (msgInQueue.getNodeId() == msg.getNodeId() && msgInQueue.getChildId() == msg.getChildId()
                        && msgInQueue.getMsgType() == msg.getMsgType() && msgInQueue.getSubType() == msg.getSubType()
                        && msgInQueue.getAck() == msg.getAck() && msgInQueue.getMsg().equals(msg.getMsg())) {
                    iterator.remove();
                } else {
                    logger.debug("Message NOT removed: {}", msg.getDebugInfo());
                }
            }
        }

        pauseWriter = false;
    }

    /**
     * Remove all messages in the smartsleep queue for a corresponding nodeId / childId combination
     *
     * @param nodeId the nodeId which messages should be deleted.
     * @param childId the childId which messages should be deleted.
     */
    private void removeSmartSleepMessage(int nodeId, int childId) {
        Iterator<MySensorsMessage> iterator = smartSleepMessageQueue.iterator();
        if (iterator != null) {
            while (iterator.hasNext()) {
                MySensorsMessage msgInQueue = iterator.next();
                if (msgInQueue.getNodeId() == nodeId && msgInQueue.getChildId() == childId) {
                    iterator.remove();
                } else {
                    logger.debug("Message NOT removed for nodeId: {} and childId: {}.", nodeId, childId);
                }
            }
        }
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
        logger.debug("Request disconnection flag setted to: {}", flag);
        requestDisconnection = flag;
    }

    /**
     * Checks if a message is in the smartsleep queue and adds it to the outbound queues
     *
     * @param nodeId of the messages that should be send immediately
     */
    public void checkPendingSmartSleepMessage(int nodeId) {
        Iterator<MySensorsMessage> iterator = smartSleepMessageQueue.iterator();
        if (iterator != null) {

            while (iterator.hasNext()) {
                MySensorsMessage msgInQueue = iterator.next();
                if (msgInQueue.getNodeId() == nodeId) {
                    iterator.remove();
                    addMySensorsOutboundMessage(msgInQueue);
                    logger.debug("Message for nodeId: {} in queue needs to be send immediately!", nodeId);
                }
            }
        }
    }

    /**
     * Debug print of the smart sleep queue content to logs
     */
    public void printSmartSleepQueue() {
        pauseWriter = true;

        Iterator<MySensorsMessage> iterator = smartSleepMessageQueue.iterator();
        if (iterator != null) {

            logger.debug("####### START SmartSleep queue #####");
            int i = 1;
            while (iterator.hasNext()) {
                MySensorsMessage msgInQueue = iterator.next();

                logger.debug("Msg: {}, nodeId: {], childId: {}, nextSend: {}.", i, msgInQueue.getNodeId(),
                        msgInQueue.getChildId(), msgInQueue.getNextSend());
                i++;
            }
            logger.debug("####### END SmartSleep queue #####");
        }
        pauseWriter = false;
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

    private void handleAckReceived(MySensorsMessage msg) {
        logger.debug(String.format("ACK received! Node: %d, Child: %d", msg.getNodeId(), msg.getChildId()));
        removeMySensorsOutboundMessage(msg);
    }

    /**
     * Implements the reader (IP & serial) that receives the messages from the MySensors network.
     *
     * @author Andrea Cioni
     * @author Tim Oberföll
     *
     */
    protected class MySensorsReader implements Runnable {
        private Logger logger = LoggerFactory.getLogger(MySensorsReader.class);

        private ExecutorService executor = Executors.newSingleThreadExecutor();
        private Future<?> future = null;

        private InputStream inStream = null;
        private BufferedReader reads = null;

        private boolean stopReader = false;

        private int readErrorCount = 0;

        public MySensorsReader(InputStream inStream) {
            this.readErrorCount = 0;
            this.inStream = inStream;
            this.reads = new BufferedReader(new InputStreamReader(inStream));
        }

        /**
         * Starts the reader process that will receive the messages from the MySensors network.
         */
        public void startReader() {
            future = executor.submit(this);
        }

        @Override
        public void run() {
            Thread.currentThread().setName(MySensorsReader.class.getName());
            String line = null;

            while (!stopReader) {
                // Is there something to read?

                try {
                    if (!reads.ready()) {
                        Thread.sleep(10);
                        continue;
                    }
                    line = reads.readLine();

                    // We lost connection
                    if (line == null) {
                        logger.warn("Connection to Gateway lost!");
                        requestDisconnection(true);
                        break;
                    }

                    logger.debug(line);
                    MySensorsMessage msg = MySensorsMessage.parse(line);
                    if (msg != null) {
                        // Have we get a I_VERSION message?
                        if (msg.isIVersionMessage()) {
                            iVersionMessageReceived(msg.getMsg());
                        }

                        // Is this an ACK message?
                        if (msg.getAck() == 1) {
                            handleAckReceived(msg);
                        }

                        myEventRegister.notifyMessageReceived(msg);
                    }
                } catch (InterruptedException e) {
                    logger.warn("Interrupted MySensorsReader");
                } catch (Exception e) {
                    logger.error("Exception on reading from connection", e);

                    if (readErrorCount < ERROR_COUNT_REQ_DISCONNECT) {
                        readErrorCount++;
                    } else {
                        readErrorCount = 0;
                        requestDisconnection(true);
                    }
                }

            }

        }

        /**
         * Stops the reader process of the bridge that receives messages from the MySensors network.
         */
        public void stopReader() {

            logger.debug("Stopping Reader thread");

            this.stopReader = true;

            if (future != null) {
                future.cancel(true);
                future = null;
            }

            if (executor != null) {
                executor.shutdown();
                executor.shutdownNow();
                executor = null;
            }

            try {
                if (reads != null) {
                    reads.close();
                    reads = null;
                }

                if (inStream != null) {
                    inStream.close();
                    inStream = null;
                }
            } catch (IOException e) {
                logger.error("Cannot close reader stream");
            }

        }

    }

    /**
     * Implements the writer (IP & serial) that sends messages to the MySensors network.
     *
     * @author Andrea Cioni
     * @author Tim Oberföll
     *
     */
    protected class MySensorsWriter implements Runnable {
        private Logger logger = LoggerFactory.getLogger(MySensorsWriter.class);

        private boolean stopWriting = false; // Stop the thread that sends the messages to the MySensors network
        private long lastSend = System.currentTimeMillis(); // date when the last message was sent. Messages are send
                                                            // with
                                                            // a delay in between.
        private PrintWriter outs = null;
        private OutputStream outStream = null;

        private ExecutorService executor = Executors.newSingleThreadExecutor();
        private Future<?> future = null;

        public MySensorsWriter(OutputStream outStream) {
            this.outStream = outStream;
            this.outs = new PrintWriter(outStream);
        }

        /**
         * Start the writer Process that will poll messages from the FIFO outbound queue
         * and send them to the MySensors network.
         */
        public void startWriter() {
            future = executor.submit(this);
        }

        @Override
        public void run() {
            Thread.currentThread().setName(MySensorsWriter.class.getName());
            while (!stopWriting) {
                if (!isWriterPaused()) {
                    try {
                        MySensorsMessage msg = pollMySensorsOutboundQueue();

                        if (msg != null) {
                            if (msg.getNextSend() < System.currentTimeMillis()
                                    && (lastSend + myGatewayConfig.getSendDelay()) < System.currentTimeMillis()) {
                                // if we request an ACK we will wait for it and keep the message in the queue (at the
                                // end)
                                // otherwise we remove the message from the queue
                                if (msg.getAck() == 1) {
                                    msg.setRetries(msg.getRetries() + 1);
                                    if (!(msg.getRetries() > MYSENSORS_NUMBER_OF_RETRIES)) {
                                        msg.setNextSend(System.currentTimeMillis()
                                                + MYSENSORS_RETRY_TIMES[msg.getRetries() - 1]);
                                        addMySensorsOutboundMessage(msg);
                                    } else {
                                        logger.warn("NO ACK from nodeId: {}", msg.getNodeId());
                                        /*
                                         * if (msg.getOldMsg().isEmpty() || msg.getOldMsg() == null) {
                                         * logger.warn("No old status know to revert to!");
                                         * } else if (msg.getRevert()) {
                                         * logger.debug("Reverting status!");
                                         * msg.setMsg(msg.getOldMsg());
                                         * msg.setAck(0);
                                         *
                                         * } else if (!msg.getRevert()) {
                                         * logger.debug("Not reverted due to configuration!");
                                         * }
                                         */
                                        myEventRegister.notifyAckNotReceived(msg);
                                        continue;
                                    }
                                }
                                String output = MySensorsMessage.generateAPIString(msg);
                                logger.debug("Sending to MySensors: {}", output.trim());

                                sendMessage(output);
                                lastSend = System.currentTimeMillis();
                            } else {
                                // Is not time for send again...
                                addMySensorsOutboundMessage(msg);
                            }
                        } else {
                            logger.warn("Message returned from queue is null");
                        }

                    } catch (InterruptedException e) {
                        logger.warn("Interrupted MySensorsWriter");
                    } catch (Exception e) {
                        logger.error("({}) on writing to connection, message: {}", e, getClass(), e.getMessage());
                    }
                }
            }
        }

        /**
         * Send a message to the MySensors network.
         *
         * @param output the message/string/line that should be send to the MySensors gateway.
         */
        protected void sendMessage(String output) {
            outs.println(output);
            outs.flush();
        }

        /**
         * Stops the writer process.
         */
        public void stopWriting() {

            logger.debug("Stopping Writer thread");

            this.stopWriting = true;

            if (future != null) {
                future.cancel(true);
                future = null;
            }

            if (executor != null) {
                executor.shutdown();
                executor.shutdownNow();
                executor = null;
            }

            try {
                if (outs != null) {
                    outs.flush();
                    outs.close();
                    outs = null;
                }

                if (outStream != null) {
                    outStream.close();
                    outStream = null;
                }
            } catch (IOException e) {
                logger.error("Cannot close writer stream");
            }

        }
    }

}
