package org.openhab.binding.mysensors.internal.sensors.variable;

import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;

public class MySensorsVariable_V_GUST extends MySensorsVariable {

    public MySensorsVariable_V_GUST() {
        super(MySensorsMessage.MYSENSORS_SUBTYPE_V_GUST);
    }

}
