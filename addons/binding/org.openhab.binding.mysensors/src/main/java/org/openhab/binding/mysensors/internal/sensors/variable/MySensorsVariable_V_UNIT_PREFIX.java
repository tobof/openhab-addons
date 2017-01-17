package org.openhab.binding.mysensors.internal.sensors.variable;

import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;

public class MySensorsVariable_V_UNIT_PREFIX extends MySensorsVariable {

    public MySensorsVariable_V_UNIT_PREFIX() {
        super(MySensorsMessage.MYSENSORS_SUBTYPE_V_UNIT_PREFIX);
    }

}
