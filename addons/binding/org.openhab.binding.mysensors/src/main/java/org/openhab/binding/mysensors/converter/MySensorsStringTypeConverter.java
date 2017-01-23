package org.openhab.binding.mysensors.converter;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;

public class MySensorsStringTypeConverter implements MySensorsTypeConverter {

    @Override
    public State fromString(String s) {
        return new StringType(s);
    }

}
