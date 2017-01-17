package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_POSITION;

public class MySensorsChild_S_GPS extends MySensorsChild {

    public MySensorsChild_S_GPS(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_POSITION());
    }

}
