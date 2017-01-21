package org.openhab.binding.mysensors.adapter;

import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;

public class MySensorsUpDownTypeAdapter implements MySensorsTypeAdapter {

    @Override
    public Integer typeFromChannelCommand(String channel, Command command) {
        if (channel.equals(MySensorsBindingConstants.CHANNEL_COVER)) {
            if (command.equals(UpDownType.UP)) {
                return MySensorsMessage.MYSENSORS_SUBTYPE_V_UP;
            } else if (command.equals(UpDownType.DOWN)) {
                return MySensorsMessage.MYSENSORS_SUBTYPE_V_DOWN;
            } else {
                throw new IllegalArgumentException("Invalid command (" + command + ") passed to UpDown adapter");
            }
        } else {
            throw new IllegalArgumentException("Invalid channel(" + channel + ") passed to UpDown adapter");
        }
    }

    @Override
    public State stateFromChannel(MySensorsVariable value) {
        if (value.getType() == MySensorsMessage.MYSENSORS_SUBTYPE_V_DOWN) {
            return UpDownType.DOWN;
        } else if (value.getType() == MySensorsMessage.MYSENSORS_SUBTYPE_V_UP) {
            return UpDownType.UP;
        } else {
            throw new IllegalArgumentException("");
        }
    }

    @Override
    public String fromCommand(Command state) {
        return "";
    }

    @Override
    public State fromString(String string) {
        throw new IllegalStateException(
                "UpDown type state could not determinateted from a string, use stateFromChannel");
    }

}
