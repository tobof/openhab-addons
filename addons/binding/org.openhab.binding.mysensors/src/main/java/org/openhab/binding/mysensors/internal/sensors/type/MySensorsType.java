package org.openhab.binding.mysensors.internal.sensors.type;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

public interface MySensorsType {
    public State fromString(String s);

    default public State fromCommand(Command value) {
        return fromString(value.toString());
    }
}
