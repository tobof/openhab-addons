package org.openhab.binding.mysensors.internal.event;

import java.util.EventListener;

import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;

/**
 * Handler that implement this interface receive update events from the MySensors network.
 *
 * @author Tim Oberföll
 *
 */
public interface MySensorsGatewayEventListener extends EventListener {
    default public void nodeIdReservationDone(Integer reservedId) throws Throwable {
    }

    default public void newNodeDiscovered(MySensorsNode node) throws Throwable {
    }

    default public void nodeUpdateEvent(MySensorsNode node, MySensorsChild child, MySensorsVariable var)
            throws Throwable {
    }

    default public void nodeReachStatusChanged(MySensorsNode node, boolean reach) throws Throwable {
    }

    /**
     * Procedure to notify new message from MySensorsNetwork.
     */
    default public void messageReceived(MySensorsMessage message) throws Throwable {
    }

    default public void connectionStatusUpdate(MySensorsAbstractConnection connection, boolean connected) throws Throwable {

    }
}
