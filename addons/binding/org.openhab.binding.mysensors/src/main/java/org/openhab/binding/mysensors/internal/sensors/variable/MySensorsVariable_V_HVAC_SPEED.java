package org.openhab.binding.mysensors.internal.sensors.variable;

import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;

public class MySensorsVariable_V_HVAC_SPEED extends MySensorsVariable {

    public MySensorsVariable_V_HVAC_SPEED() {
        super(MySensorsMessage.MYSENSORS_SUBTYPE_V_HVAC_SPEED);
    }

}
