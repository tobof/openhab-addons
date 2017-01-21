package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_STATUS;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_TRIPPED;

public class MySensorsChild_S_SPRINKLER extends MySensorsChild {

    public MySensorsChild_S_SPRINKLER(int childId) {
        super(childId);
        setPresentationCode(MySensorsMessage.MYSENSORS_SUBTYPE_S_SPRINKLER);
        addVariable(new MySensorsVariable_V_STATUS());
        addVariable(new MySensorsVariable_V_TRIPPED());
    }

}
