package org.openhab.binding.mysensors.adapter;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

public class MySensorsOnOffTypeAdapter implements MySensorsTypeAdapter {

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
    public String fromCommand(Command value) {
        if (value instanceof OnOffType) {
            if (value == OnOffType.OFF) {
                return "0";
            } else if (value == OnOffType.ON) {
                return "1";
            } else {
                throw new IllegalArgumentException("Passed command is not On/Off");
            }
        } else {
            throw new IllegalArgumentException("Passed command: " + value + " is not an OnOff command");
        }
    }

}
