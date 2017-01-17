package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_DISTANCE;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_UNIT_PREFIX;

public class MySensorsChild_S_DISTANCE extends MySensorsChild {

    public MySensorsChild_S_DISTANCE(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_DISTANCE());
        addVariable(new MySensorsVariable_V_UNIT_PREFIX());
    }

}
