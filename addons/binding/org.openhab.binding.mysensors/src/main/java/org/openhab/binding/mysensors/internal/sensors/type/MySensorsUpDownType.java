package org.openhab.binding.mysensors.internal.sensors.type;

import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;

public class MySensorsUpDownType implements MySensorsType {

    @Override
    public State fromMessage(MySensorsMessage msg) {
        if (msg.getSubType() == MySensorsBindingConstants.MYSENSORS_SUBTYPE_V_DOWN) {
            return UpDownType.DOWN;
        } else if (msg.getSubType() == MySensorsBindingConstants.MYSENSORS_SUBTYPE_V_UP) {
            return UpDownType.UP;
        }

        return MySensorsType.super.fromMessage(msg);

    }

    @Override
    public State fromString(String s) {
        return UnDefType.UNDEF;
    }

}
