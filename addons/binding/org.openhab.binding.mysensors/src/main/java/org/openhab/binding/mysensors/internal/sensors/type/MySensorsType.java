package org.openhab.binding.mysensors.internal.sensors.type;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;

public interface MySensorsType {

    default public State fromMessage(MySensorsMessage msg) {
        return fromString(msg.msg);
    }

    public State fromString(String s);

    default public State fromCommand(Command value) {
        return fromString(value.toString());
    }

    default public String toPayloadString(State state) {
        return state.toString();
    }

    default public Integer toSubtypeInt(State state) {
        return null;
    }
}
