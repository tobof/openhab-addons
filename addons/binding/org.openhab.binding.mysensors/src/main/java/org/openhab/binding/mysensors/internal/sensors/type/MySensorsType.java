package org.openhab.binding.mysensors.internal.sensors.type;

import org.eclipse.smarthome.core.types.State;

public interface MySensorsType {
    public State fromString(String s);
}
