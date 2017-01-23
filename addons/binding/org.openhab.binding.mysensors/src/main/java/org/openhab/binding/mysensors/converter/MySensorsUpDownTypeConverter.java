package org.openhab.binding.mysensors.converter;

import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;

public class MySensorsUpDownTypeConverter implements MySensorsTypeConverter {

    @Override
    public Integer typeFromChannelCommand(String channel, Command command) {
        if (channel.equals(MySensorsBindingConstants.CHANNEL_COVER)) {
            if (command.equals(UpDownType.UP)) {
                return MySensorsMessage.MYSENSORS_SUBTYPE_V_UP;
            } else if (command.equals(UpDownType.DOWN)) {
                return MySensorsMessage.MYSENSORS_SUBTYPE_V_DOWN;
            } else if (command instanceof StopMoveType) {
                if (command.equals(StopMoveType.STOP)) {
                    return MySensorsMessage.MYSENSORS_SUBTYPE_V_STOP;
                } else {
                    throw new IllegalArgumentException("Invalid command of type StopMoveType");
                }
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
            throw new IllegalArgumentException("Variable " + value.getType() + " is not up/down");
        }
    }

    @Override
    public String fromCommand(Command state) {
        if (state instanceof UpDownType) {
            if (state == UpDownType.DOWN || state == UpDownType.UP) {
                return "1";
            } else {
                throw new IllegalStateException("Invalid UpDown state: " + state);
            }
        } else if (state instanceof StopMoveType) {
            return "1";
        } else {
            throw new IllegalStateException("UpDown command is the only one command allowed by this adapter, passed: "
                    + state + "(" + (state != null ? state.getClass() : "") + ")");
        }
    }

    @Override
    public State fromString(String string) {
        throw new IllegalStateException(
                "UpDown type state could not determinateted from a string, use stateFromChannel");
    }

}
