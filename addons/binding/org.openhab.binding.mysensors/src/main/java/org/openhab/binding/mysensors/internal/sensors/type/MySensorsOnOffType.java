package org.openhab.binding.mysensors.internal.sensors.type;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

public class MySensorsOnOffType implements MySensorsType {

    @Override
    public State fromString(String s) {
        if ("0".equals(s)) {
            return OnOffType.OFF;
        } else if ("1".equals(s)) {
            return OnOffType.ON;
        } else {
            throw new IllegalArgumentException("String: " + s + ", could not be used as OnOff state");
        }
    }

    @Override
    public State fromCommand(Command value) {
        if (value instanceof OnOffType) {
            if (value == OnOffType.OFF) {
                return OnOffType.OFF;
            } else if (value == OnOffType.ON) {
                return OnOffType.ON;
            } else {
                throw new IllegalArgumentException("Passed command is not On/Off");
            }
        } else {
            throw new IllegalArgumentException("Passed command: " + value + " is not an OnOff command");
        }
    }

}
