package org.openhab.binding.mysensors.internal.sensors.type;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

public class MySensorsOpenCloseType implements MySensorsType {
    @Override
    public State fromString(String s) {
        if ("0".equals(s)) {
            return OpenClosedType.CLOSED;
        } else if ("1".equals(s)) {
            return OpenClosedType.OPEN;
        } else {
            throw new IllegalArgumentException("String: " + s + ", could not be used as OpenClose state");
        }
    }

    @Override
    public State fromCommand(Command value) {
        if (value instanceof OnOffType) {
            if (value == OpenClosedType.CLOSED) {
                return OpenClosedType.CLOSED;
            } else if (value == OpenClosedType.OPEN) {
                return OpenClosedType.OPEN;
            } else {
                throw new IllegalArgumentException("Passed command is not Open/Closed");
            }
        } else {
            throw new IllegalArgumentException("Passed command: " + value + " is not an OpenClose command");
        }
    }
}
