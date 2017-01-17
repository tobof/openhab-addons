package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_LEVEL;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_UNIT_PREFIX;

public class MySensorsChild_S_AIR_QUALITY extends MySensorsChild {

    public MySensorsChild_S_AIR_QUALITY(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_LEVEL());
        addVariable(new MySensorsVariable_V_UNIT_PREFIX());
    }

}
