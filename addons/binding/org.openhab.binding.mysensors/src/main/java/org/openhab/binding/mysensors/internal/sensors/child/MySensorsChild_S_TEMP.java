package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_ID;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_TEMP;

public class MySensorsChild_S_TEMP extends MySensorsChild {

    public MySensorsChild_S_TEMP(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_TEMP());
        addVariable(new MySensorsVariable_V_ID());
    }

}
