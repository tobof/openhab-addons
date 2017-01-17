package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_RAIN;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_RAINRATE;

public class MySensorsChild_S_RAIN extends MySensorsChild {

    public MySensorsChild_S_RAIN(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_RAIN());
        addVariable(new MySensorsVariable_V_RAINRATE());
    }

}
