package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_KWH;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_POWER_FACTOR;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_VA;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_VAR;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_WATT;

public class MySensorsChild_S_POWER extends MySensorsChild {

    public MySensorsChild_S_POWER(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_WATT());
        addVariable(new MySensorsVariable_V_KWH());
        addVariable(new MySensorsVariable_V_VAR());
        addVariable(new MySensorsVariable_V_VA());
        addVariable(new MySensorsVariable_V_POWER_FACTOR());
    }

}
