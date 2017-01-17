package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_TEXT;

public class MySensorsChild_S_INFO extends MySensorsChild {

    public MySensorsChild_S_INFO(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_TEXT());
    }

}
