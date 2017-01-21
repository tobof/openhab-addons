package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;

public class MySensorsChild_S_ARDUINO_REPEATER_NODE extends MySensorsChild {

    public MySensorsChild_S_ARDUINO_REPEATER_NODE(int childId) {
        super(childId);
        setPresentationCode(MySensorsMessage.MYSENSORS_SUBTYPE_S_ARDUINO_REPEATER_NODE);

    }

}
