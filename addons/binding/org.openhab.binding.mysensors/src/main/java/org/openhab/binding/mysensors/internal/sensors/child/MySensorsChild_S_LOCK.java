package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_LOCK_STATUS;

public class MySensorsChild_S_LOCK extends MySensorsChild {

    public MySensorsChild_S_LOCK(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_LOCK_STATUS());
    }

}
