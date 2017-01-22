package org.openhab.binding.mysensors.adapter;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;

public class MySensorsPercentTypeConverter implements MySensorsTypeConverter {

    @Override
    public State fromString(String s) {
        return new PercentType(s);
    }
}
