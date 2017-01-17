package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_IR_RECEIVE;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_IR_RECORD;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_IR_SEND;

public class MySensorsChild_S_IR extends MySensorsChild {

    public MySensorsChild_S_IR(int childId) {
        super(childId);
        addVariable(new MySensorsVariable_V_IR_SEND());
        addVariable(new MySensorsVariable_V_IR_RECEIVE());
        addVariable(new MySensorsVariable_V_IR_RECORD());
    }

}
