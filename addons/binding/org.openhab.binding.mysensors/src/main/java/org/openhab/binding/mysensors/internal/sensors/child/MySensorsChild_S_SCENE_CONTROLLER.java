package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_SCENE_OFF;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_SCENE_ON;

public class MySensorsChild_S_SCENE_CONTROLLER extends MySensorsChild {

    public MySensorsChild_S_SCENE_CONTROLLER(int childId) {
        super(childId);
        setPresentationCode(MySensorsMessage.MYSENSORS_SUBTYPE_S_SCENE_CONTROLLER);
        addVariable(new MySensorsVariable_V_SCENE_ON());
        addVariable(new MySensorsVariable_V_SCENE_OFF());
    }

}