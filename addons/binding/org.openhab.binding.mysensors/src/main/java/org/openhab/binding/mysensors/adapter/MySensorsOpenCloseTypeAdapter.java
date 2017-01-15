package org.openhab.binding.mysensors.adapter;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

public class MySensorsOpenCloseTypeAdapter implements MySensorsTypeAdapter {
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
    public String fromCommand(Command value) {
        if (value instanceof OnOffType) {
            if (value == OpenClosedType.CLOSED) {
                return "0";
            } else if (value == OpenClosedType.OPEN) {
                return "1";
            } else {
                throw new IllegalArgumentException("Passed command is not Open/Closed");
            }
        } else {
            throw new IllegalArgumentException("Passed command: " + value + " is not an OpenClose command");
        }
    }

}
