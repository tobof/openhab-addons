package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_ARMED;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_TRIPPED;

public class MySensorsChild_S_DOOR extends MySensorsChild {

    public MySensorsChild_S_DOOR(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_TRIPPED());
        addVariable(new MySensorsVariable_V_ARMED());
    }

}
