package org.openhab.binding.mysensors.adapter;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;

public class MySensorsDecimalTypeConverter implements MySensorsTypeConverter {

    @Override
    public State fromString(String s) {
        return new DecimalType(s);
    }
}
