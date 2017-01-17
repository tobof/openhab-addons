package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_EC;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_ORP;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_PH;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_STATUS;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_TEMP;

public class MySensorsChild_S_WATER_QUALITY extends MySensorsChild {

    public MySensorsChild_S_WATER_QUALITY(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_TEMP());
        addVariable(new MySensorsVariable_V_PH());
        addVariable(new MySensorsVariable_V_ORP());
        addVariable(new MySensorsVariable_V_EC());
        addVariable(new MySensorsVariable_V_STATUS());
    }

}
