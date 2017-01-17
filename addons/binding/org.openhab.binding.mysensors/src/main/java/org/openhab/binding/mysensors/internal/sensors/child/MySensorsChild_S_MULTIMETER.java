package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_CURRENT;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_IMPEDANCE;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_VOLTAGE;

public class MySensorsChild_S_MULTIMETER extends MySensorsChild {

    public MySensorsChild_S_MULTIMETER(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_VOLTAGE());
        addVariable(new MySensorsVariable_V_CURRENT());
        addVariable(new MySensorsVariable_V_IMPEDANCE());
    }

}
