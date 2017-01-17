package org.openhab.binding.mysensors.internal.sensors.variable;

import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;

public class MySensorsVariable_V_UP extends MySensorsVariable {

    public MySensorsVariable_V_UP() {
        super(MySensorsMessage.MYSENSORS_SUBTYPE_V_UP);
    }

}
