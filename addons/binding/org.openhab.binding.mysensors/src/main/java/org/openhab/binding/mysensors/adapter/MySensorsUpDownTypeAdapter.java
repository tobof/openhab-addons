package org.openhab.binding.mysensors.adapter;

import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;

public class MySensorsUpDownTypeAdapter implements MySensorsTypeAdapter {

    @Override
    public State stateFromChannel(MySensorsVariable value) {
        if (value.getType() == MySensorsBindingConstants.MYSENSORS_SUBTYPE_V_DOWN) {
            return UpDownType.DOWN;
        } else if (value.getType() == MySensorsBindingConstants.MYSENSORS_SUBTYPE_V_UP) {
            return UpDownType.UP;
        } else {
            throw new IllegalArgumentException("");
        }
    }

    @Override
    public String fromCommand(Command state) {
        return "";
    }

    @Override
    public State fromString(String string) {
        throw new IllegalStateException(
                "UpDown type state could not determinateted from a string, use stateFromChannel");
    }

}
