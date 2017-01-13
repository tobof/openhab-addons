package org.openhab.binding.mysensors.internal.event;

import java.util.List;

import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySensorsEventRegister extends EventRegister<MySensorsGatewayEventListener> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private EventRegister<MySensorsGatewayEventListener> eventRegister;

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

    public void notifyNewNodeDiscovered(MySensorsNode node) {
        synchronized (eventRegister.getEventListeners()) {
            for (MySensorsGatewayEventListener listener : eventRegister.getEventListeners()) {
                logger.trace("Broadcasting event {} to: {}", node, listener);

                try {
                    listener.newNodeDiscovered(node);
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

    public void notifyNodeUpdateEvent(MySensorsNode node, MySensorsChild child, MySensorsVariable variable) {
        synchronized (eventRegister.getEventListeners()) {
            for (MySensorsGatewayEventListener listener : eventRegister.getEventListeners()) {
                logger.trace("Broadcasting event {} to: {}", variable, listener);

                try {
                    listener.nodeUpdateEvent(node, child, variable);
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

}
