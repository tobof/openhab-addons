package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_PERCENTAGE;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_STATUS;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_WATT;

public class MySensorsChild_S_DIMMER extends MySensorsChild {

    public MySensorsChild_S_DIMMER(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_STATUS());
        addVariable(new MySensorsVariable_V_PERCENTAGE());
        addVariable(new MySensorsVariable_V_WATT());
    }

}
