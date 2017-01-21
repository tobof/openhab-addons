package org.openhab.binding.mysensors.adapter;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;

public interface MySensorsTypeAdapter {

    default public State stateFromChannel(MySensorsVariable value) {
        return fromString(value.getValue());
    }

    public State fromString(String string);

    default public String fromCommand(Command command) {
        return command.toString();
    }

    default int typeFromChannelCommand(String channel, Command command) {
        return MySensorsBindingConstants.INVERSE_CHANNEL_MAP.get(command);
    }
}
