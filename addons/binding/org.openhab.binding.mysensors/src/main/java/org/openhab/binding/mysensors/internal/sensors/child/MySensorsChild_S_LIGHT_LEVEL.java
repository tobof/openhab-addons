package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_LEVEL;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_LIGHT_LEVEL;

public class MySensorsChild_S_LIGHT_LEVEL extends MySensorsChild {

    public MySensorsChild_S_LIGHT_LEVEL(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_LIGHT_LEVEL());
        addVariable(new MySensorsVariable_V_LEVEL());
    }

}
