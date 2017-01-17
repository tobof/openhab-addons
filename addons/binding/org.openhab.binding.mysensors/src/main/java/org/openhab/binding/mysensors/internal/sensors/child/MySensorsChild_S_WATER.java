package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_FLOW;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_VOLUME;

public class MySensorsChild_S_WATER extends MySensorsChild {

    public MySensorsChild_S_WATER(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_FLOW());
        addVariable(new MySensorsVariable_V_VOLUME());
    }

}
