package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_IMPEDANCE;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_WEIGHT;

public class MySensorsChild_S_WEIGHT extends MySensorsChild {

    public MySensorsChild_S_WEIGHT(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_WEIGHT());
        addVariable(new MySensorsVariable_V_IMPEDANCE());
    }

}
