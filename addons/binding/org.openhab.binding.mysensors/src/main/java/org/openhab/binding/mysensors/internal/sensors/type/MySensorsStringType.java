package org.openhab.binding.mysensors.internal.sensors.type;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;

public class MySensorsStringType extends StringType implements MySensorsType {

    @Override
    public State fromString(String s) {
        return new StringType(s);
    }

}
