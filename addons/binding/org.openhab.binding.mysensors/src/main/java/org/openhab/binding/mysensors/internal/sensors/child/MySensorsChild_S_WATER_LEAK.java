package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_ARMED;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_TRIPPED;

public class MySensorsChild_S_WATER_LEAK extends MySensorsChild {

    public MySensorsChild_S_WATER_LEAK(int childId) {
        super(childId);
        setPresentationCode(MySensorsMessage.MYSENSORS_SUBTYPE_S_WATER_LEAK);
        addVariable(new MySensorsVariable_V_TRIPPED());
        addVariable(new MySensorsVariable_V_ARMED());
    }

}
