package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_DOWN;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_PERCENTAGE;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_STOP;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_UP;

public class MySensorsChild_S_COVER extends MySensorsChild {

    public MySensorsChild_S_COVER(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_UP());
        addVariable(new MySensorsVariable_V_DOWN());
        addVariable(new MySensorsVariable_V_STOP());
        addVariable(new MySensorsVariable_V_PERCENTAGE());
    }

}
