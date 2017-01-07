package org.openhab.binding.mysensors.internal.event;

import java.util.EventListener;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;

public interface MySensorsDeviceEventListener extends EventListener {
    default public void nodeIdReservationDone(Integer reservedId) throws Throwable {
    }

    default public void newNodeDiscovered(MySensorsNode node) throws Throwable {
    }

    default public void nodeUpdateEvent(MySensorsNode node, MySensorsChild child, MySensorsVariable var)
            throws Throwable {
    }

    default public void nodeReachStatusChanged(MySensorsNode node, boolean reach) throws Throwable {
    }
}
