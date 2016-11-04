package org.openhab.binding.mysensors.internal.sensors.type;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;

public class MySensorsPercentType extends DecimalType implements MySensorsType {
    /**
     *
     */
    private static final long serialVersionUID = -9040518556535682773L;

    @Override
    public State fromString(String s) {
        return new PercentType(s);
    }
}
