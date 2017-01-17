package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_RGB;

public class MySensorsChild_S_COLOR_SENSOR extends MySensorsChild {

    public MySensorsChild_S_COLOR_SENSOR(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_RGB());
    }

}
