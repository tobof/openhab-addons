package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_RGBW;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_WATT;

public class MySensorsChild_S_RGBW_LIGHT extends MySensorsChild {

    public MySensorsChild_S_RGBW_LIGHT(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_RGBW());
        addVariable(new MySensorsVariable_V_WATT());
    }

}
