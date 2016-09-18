/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.handler.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.handler.MySensorsUpdateListener;
import org.openhab.binding.mysensors.protocol.MySensorsReader;
import org.openhab.binding.mysensors.protocol.MySensorsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MySensorsBridgeConnection {

    private Logger logger = LoggerFactory.getLogger(MySensorsBridgeConnection.class);

    private boolean pauseWriter = false;

    private BlockingQueue<MySensorsMessage> outboundMessageQueue = null;

    private boolean connected = false;

    private boolean requestDisconnection = false;

    private MySensorsBridgeConnection waitingObj = null;
    private boolean iVersionResponse = false;

    private boolean skipStartupCheck = false;

    protected MySensorsWriter mysConWriter = null;
    protected MySensorsReader mysConReader = null;

    // Update listener
    private List<MySensorsUpdateListener> updateListeners = null;

    public MySensorsBridgeConnection(boolean skipStartupCheck) {
        outboundMessageQueue = new LinkedBlockingQueue<MySensorsMessage>();
        this.skipStartupCheck = skipStartupCheck;
        updateListeners = new ArrayList<>();
    }

    /**
     * Startup connection with bridge
     *
     * @return
     */
    public boolean connect() {
        connected = _connect();
        return connected;
    }

    public abstract boolean _connect();

    /**
     * Shutdown method that allows the correct disconnection with the used bridge
     *
     * @return
     */
    public void disconnect() {
        removeAllUpdateListener();
        clearOutboundMessagesQueue();
        _disconnect();
        connected = false;
    }

    public abstract void _disconnect();

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
            logger.error("Cannot start reading/writing thread, probably sync message (I_VERSION) not received");
        }

        return iVersionResponse;
    }

    public MySensorsMessage pollMySensorsOutboundQueue() throws InterruptedException {
        return outboundMessageQueue.poll(1, TimeUnit.DAYS);
    }

    private void clearOutboundMessagesQueue() {
        synchronized (outboundMessageQueue) {
            outboundMessageQueue.clear();
        }
    }

    public void addMySensorsOutboundMessage(MySensorsMessage msg) {
        addMySensorsOutboundMessage(msg, 1);
    }

    public void addMySensorsOutboundMessage(MySensorsMessage msg, int copy) {
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
     * @param listener An Object, that wants to listen on status updates
     */
    public void addUpdateListener(MySensorsUpdateListener listener) {
        synchronized (updateListeners) {
            if (!updateListeners.contains(listener)) {
                updateListeners.add(listener);
            }
        }
    }

    public void removeUpdateListener(MySensorsUpdateListener listener) {
        synchronized (updateListeners) {
            if (updateListeners.contains(listener)) {
                updateListeners.remove(listener);
            }
        }
    }

    private void removeAllUpdateListener() {
        synchronized (updateListeners) {
            updateListeners.clear();
        }
    }

    public List<MySensorsUpdateListener> getUpdateListeners() {
        return updateListeners;
    }

    public void broadCastEvent(MySensorsStatusUpdateEvent event) {
        synchronized (updateListeners) {
            for (MySensorsUpdateListener mySensorsEventListener : updateListeners) {
                mySensorsEventListener.statusUpdateReceived(event);
            }
        }
    }

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

    public void iVersionMessageReceived(String msg) {
        if (waitingObj != null) {
            logger.debug("Good,Gateway is up and running! (Ver:{})", msg);
            synchronized (waitingObj) {
                iVersionResponse = true;
                waitingObj.notifyAll();
                waitingObj = null;
            }
        }
    }

    public boolean isWriterPaused() {
        return pauseWriter;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean requestingDisconnection() {
        return requestDisconnection;
    }

    public void requestDisconnection(boolean flag) {
        logger.debug("Request disconnection flag setted to: " + flag);
        requestDisconnection = flag;
    }
}
