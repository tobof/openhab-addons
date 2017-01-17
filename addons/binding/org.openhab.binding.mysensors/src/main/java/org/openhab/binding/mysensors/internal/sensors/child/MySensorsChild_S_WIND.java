package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_DIRECTION;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_GUST;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_WIND;

public class MySensorsChild_S_WIND extends MySensorsChild {

    public MySensorsChild_S_WIND(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_WIND());
        addVariable(new MySensorsVariable_V_GUST());
        addVariable(new MySensorsVariable_V_DIRECTION());
    }

}
