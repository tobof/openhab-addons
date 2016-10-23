package org.openhab.binding.mysensors.internal.sensors;

import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.mysensors.internal.sensors.type.MySensorsType;

public class MySensorsInternal extends MySensorsVariable {

    public MySensorsInternal(int variableNum, MySensorsType type) {
        super(variableNum, type);
    }

    public MySensorsInternal(int variableNum, MySensorsType type, State initValue) {
        super(variableNum, type, initValue);
    }

}
