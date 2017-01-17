package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_FLOW;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_UNIT_PREFIX;

public class MySensorsChild_S_GAS extends MySensorsChild {

    public MySensorsChild_S_GAS(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_FLOW());
        addVariable(new MySensorsVariable_V_UNIT_PREFIX());
    }

}
