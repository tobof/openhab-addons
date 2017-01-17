package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_STATUS;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_WATT;

public class MySensorsChild_S_BINARY extends MySensorsChild {

    public MySensorsChild_S_BINARY(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_STATUS());
        addVariable(new MySensorsVariable_V_WATT());
    }

}
