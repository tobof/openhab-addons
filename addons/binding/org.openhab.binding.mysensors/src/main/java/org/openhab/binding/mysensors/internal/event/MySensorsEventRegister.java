/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.event;

import java.util.List;

import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Cioni
 *
 */
public class MySensorsEventRegister extends EventRegister<MySensorsGatewayEventListener> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private EventRegister<MySensorsGatewayEventListener> eventRegister;

    // TODO #1 send update only if necessary (e.g.: status update true->false, false->true)
    // TODO #2 asynchronous message notify

    public MySensorsEventRegister() {
        eventRegister = new EventRegister<>();
    }

    @Override
    public void addEventListener(MySensorsGatewayEventListener listener) {
        eventRegister.addEventListener(listener);

    }

    @Override
    public void clearAllListeners() {
        eventRegister.clearAllListeners();

    }

    @Override
    public List<MySensorsGatewayEventListener> getEventListeners() {
        return eventRegister.getEventListeners();
    }

    @Override
    public boolean isEventListenerRegisterd(MySensorsGatewayEventListener listener) {
        return eventRegister.isEventListenerRegisterd(listener);
    }

    @Override
    public void removeEventListener(MySensorsGatewayEventListener listener) {
        eventRegister.removeEventListener(listener);
    }

    public void notifyBridgeStatusUpdate(MySensorsAbstractConnection connection, boolean connected) {
        synchronized (eventRegister.getEventListeners()) {
            for (MySensorsGatewayEventListener listener : eventRegister.getEventListeners()) {
                logger.trace("Broadcasting event {} to: {}", connection.toString(), listener);

                try {
                    listener.connectionStatusUpdate(connection, connected);
                } catch (Throwable e) {
                    logger.error("Event broadcasting throw an exception", e);
                }
            }
        }
    }

    public void notifyMessageReceived(MySensorsMessage msg) {
        synchronized (eventRegister.getEventListeners()) {
            for (MySensorsGatewayEventListener listener : eventRegister.getEventListeners()) {
                logger.trace("Broadcasting event {} to: {}", msg, listener);

                try {
                    listener.messageReceived(msg);
                } catch (Throwable e) {
                    logger.error("Event broadcasting throw an exception", e);
                }
            }
        }

    }

    public void notifyNewNodeDiscovered(MySensorsNode node, MySensorsChild child) {
        synchronized (eventRegister.getEventListeners()) {
            for (MySensorsGatewayEventListener listener : eventRegister.getEventListeners()) {
                logger.trace("Broadcasting event {} to: {}", node, listener);

                try {
                    listener.newNodeDiscovered(node, child);
                } catch (Throwable e) {
                    logger.error("Event broadcasting throw an exception", e);
                }
            }
        }
    }

    public void notifyNodeIdReserved(Integer reserved) {
        synchronized (eventRegister.getEventListeners()) {
            for (MySensorsGatewayEventListener listener : eventRegister.getEventListeners()) {
                logger.trace("Broadcasting event {} to: {}", reserved, listener);

                try {
                    listener.nodeIdReservationDone(reserved);
                } catch (Throwable e) {
                    logger.error("Event broadcasting throw an exception", e);
                }
            }
        }
    }

    public void notifyNodeUpdateEvent(MySensorsNode node, MySensorsChild child, MySensorsVariable variable,
            MySensorsNodeUpdateEventType eventType) {
        synchronized (eventRegister.getEventListeners()) {
            for (MySensorsGatewayEventListener listener : eventRegister.getEventListeners()) {
                logger.trace("Broadcasting event {} to: {}", (variable != null ? variable : node), listener);

                try {
                    listener.sensorUpdateEvent(node, child, variable, eventType);
                } catch (Throwable e) {
                    logger.error("Event broadcasting throw an exception", e);
                }
            }
        }
    }

    public void notifyNodeReachEvent(MySensorsNode node, boolean reach) {
        synchronized (eventRegister.getEventListeners()) {
            for (MySensorsGatewayEventListener listener : eventRegister.getEventListeners()) {
                logger.trace("Broadcasting event {} to: {}", node, listener);

                try {
                    listener.nodeReachStatusChanged(node, reach);
                } catch (Throwable e) {
                    logger.error("Event broadcasting throw an exception", e);
                }
            }
        }
    }

    public void notifyAckNotReceived(MySensorsMessage msg) {
        synchronized (eventRegister.getEventListeners()) {
            for (MySensorsGatewayEventListener listener : eventRegister.getEventListeners()) {
                logger.trace("Broadcasting event {} to: {}", msg, listener);

                try {
                    listener.ackNotReceived(msg);
                } catch (Throwable e) {
                    logger.error("Event broadcasting throw an exception", e);
                }
            }
        }

    }

}
