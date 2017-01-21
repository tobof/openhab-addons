package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;

public class MySensorsChild_S_CUSTOM extends MySensorsChild {

    public MySensorsChild_S_CUSTOM(int childId) {
        super(childId);
        setPresentationCode(MySensorsMessage.MYSENSORS_SUBTYPE_S_CUSTOM);
    }

}
