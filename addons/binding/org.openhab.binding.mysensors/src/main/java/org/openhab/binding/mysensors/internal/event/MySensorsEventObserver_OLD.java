package org.openhab.binding.mysensors.internal.event;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.mysensors.internal.protocol.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySensorsEventObserver_OLD {

    private static Logger logger = LoggerFactory.getLogger(MySensorsEventObserver_OLD.class);

    // Update listener
    private static List<MySensorsUpdateListener> updateListeners = new ArrayList<>();

    public static boolean isEventListenerRegisterd(MySensorsUpdateListener listener) {
        boolean ret = false;
        synchronized (updateListeners) {
            ret = updateListeners.contains(listener);
        }

        return ret;
    }

    /**
     * @param listener An Object, that wants to listen on status updates
     */
    public static void addEventListener(MySensorsUpdateListener listener) {
        synchronized (updateListeners) {
            if (!updateListeners.contains(listener)) {
                logger.trace("Adding listener: " + listener);
                updateListeners.add(listener);
            }
        }
    }

    public static void removeEventListener(MySensorsUpdateListener listener) {
        synchronized (updateListeners) {
            if (updateListeners.contains(listener)) {
                logger.trace("Removing listener: " + listener);
                updateListeners.remove(listener);
            }
        }
    }

    public static void clearAllListeners() {
        synchronized (updateListeners) {
            updateListeners.clear();
        }
    }

    public static List<MySensorsUpdateListener> getEventListeners() {
        return updateListeners;
    }

    public static void notifyMessageReceived(MySensorsMessage message) {
        synchronized (updateListeners) {
            for (MySensorsUpdateListener mySensorsEventListener : updateListeners) {
                logger.trace("Broadcasting event {} to: {}", message.toString(), mySensorsEventListener);
                try {
                    mySensorsEventListener.messageReceived(message);
                } catch (Throwable e) {
                    logger.error("Event broadcasting throw an exception", e);
                }
            }
        }
    }

    public static void notifyNodeIdReserved(Integer reserved) {
        synchronized (updateListeners) {
            for (MySensorsUpdateListener mySensorsEventListener : updateListeners) {
                logger.trace("Broadcasting event {} to: {}", reserved.toString(), mySensorsEventListener);
                try {
                    mySensorsEventListener.nodeIdReservationDone(reserved);
                } catch (Throwable e) {
                    logger.error("Event broadcasting throw an exception", e);
                }
            }
        }
    }

    public static void notifyNewNodeDiscovered(MySensorsNode node) {
        synchronized (updateListeners) {
            for (MySensorsUpdateListener mySensorsEventListener : updateListeners) {
                logger.trace("Broadcasting event {} to: {}", node.toString(), mySensorsEventListener);
                try {
                    mySensorsEventListener.newNodeDiscovered(node);
                } catch (Throwable e) {
                    logger.error("Event broadcasting throw an exception", e);
                }
            }
        }
    }

    public static void notifyNodeUpdateEvent(MySensorsNode node, MySensorsChild child, MySensorsVariable var) {
        synchronized (updateListeners) {
            for (MySensorsUpdateListener mySensorsEventListener : updateListeners) {
                logger.trace("Broadcasting event {} to: {}", var.toString(), mySensorsEventListener);
                try {
                    mySensorsEventListener.nodeUpdateEvent(node, child, var);
                } catch (Throwable e) {
                    logger.error("Event broadcasting throw an exception", e);
                }
            }
        }
    }

    public static void notifyNodeReachStatusChanged(MySensorsNode node, boolean reach) {
        synchronized (updateListeners) {
            for (MySensorsUpdateListener mySensorsEventListener : updateListeners) {
                logger.trace("Broadcasting event {} to: {}", node.toString(), mySensorsEventListener);
                try {
                    mySensorsEventListener.nodeReachStatusChanged(node, reach);
                } catch (Throwable e) {
                    logger.error("Event broadcasting throw an exception", e);
                }
            }
        }
    }

    public static void notifyBridgeStatusUpdate(MySensorsBridgeConnection connection, boolean connected) {
        synchronized (updateListeners) {
            for (MySensorsUpdateListener mySensorsEventListener : updateListeners) {
                logger.trace("Broadcasting event {} to: {}", connection.toString(), mySensorsEventListener);
                try {
                    mySensorsEventListener.bridgeStatusUpdate(connection, connected);
                } catch (Throwable e) {
                    logger.error("Event broadcasting throw an exception", e);
                }
            }
        }
    }
}
