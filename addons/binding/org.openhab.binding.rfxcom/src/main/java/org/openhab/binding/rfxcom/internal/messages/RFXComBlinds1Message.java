/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * RFXCOM data class for blinds1 message.
 *
 * @author Peter Janson / Pål Edman - Initial contribution
 * @author Pauli Anttila
 */
public class RFXComBlinds1Message extends RFXComBaseMessage {

    public enum Commands {
        OPEN(0), // MediaMount DOWN(0),
        CLOSE(1), // MediaMount UPP(1),
        STOP(2),
        CONFIRM(3),
        SET_LIMIT(4), // YR1326 SET_UPPER_LIMIT(4),
        SET_LOWER_LIMIT(5), // YR1326
        DELETE_LIMITS(6), // YR1326
        CHANGE_DIRECTON(7), // YR1326

        UNKNOWN(255);

        private final int command;

        Commands(int command) {
            this.command = command;
        }

        Commands(byte command) {
            this.command = command;
        }

        public byte toByte() {
            return (byte) command;
        }
    }

    public enum SubType {
        T0(0),
        T1(1),
        T2(2),
        T3(3),
        T4(4),
        T5(5),
        T6(6),
        T7(7),
        T8(8),

        UNKNOWN(255);

        private final int subType;

        SubType(int subType) {
            this.subType = subType;
        }

        SubType(byte subType) {
            this.subType = subType;
        }

        public byte toByte() {
            return (byte) subType;
        }
    }

    private final static List<RFXComValueSelector> supportedInputValueSelectors = Arrays
            .asList(RFXComValueSelector.SIGNAL_LEVEL, RFXComValueSelector.BATTERY_LEVEL, RFXComValueSelector.COMMAND);

    private final static List<RFXComValueSelector> supportedOutputValueSelectors = Arrays
            .asList(RFXComValueSelector.SHUTTER);

    public SubType subType = SubType.T0;
    public int sensorId = 0;
    public byte unitCode = 0;
    public Commands command = Commands.STOP;
    public byte signalLevel = 0;
    public byte batteryLevel = 0;

    public RFXComBlinds1Message() {
        packetType = PacketType.BLINDS1;
    }

    public RFXComBlinds1Message(byte[] data) {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Device Id = " + getDeviceId();
        str += ", Command = " + command;
        str += ", Signal level = " + signalLevel;
        str += ", Battery level = " + batteryLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) {

        super.encodeMessage(data);

        try {
            subType = SubType.values()[super.subType];
        } catch (Exception e) {
            subType = SubType.UNKNOWN;
        }

        sensorId = (data[4] & 0xFF) << 16 | (data[5] & 0xFF) << 8 | (data[6] & 0xFF);
        unitCode = data[7];

        try {
            command = Commands.values()[data[8]];
        } catch (Exception e) {
            command = Commands.UNKNOWN;
        }

        signalLevel = (byte) ((data[9] & 0xF0) >> 4);
        batteryLevel = (byte) (data[9] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        // Example data
        // BLINDS1 09 19 00 06 00 B1 8F 01 00 70

        byte[] data = new byte[10];

        data[0] = 0x09;
        data[1] = RFXComBaseMessage.PacketType.BLINDS1.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((sensorId >> 16) & 0xFF);
        data[5] = (byte) ((sensorId >> 8) & 0xFF);
        data[6] = (byte) (sensorId & 0xFF);
        data[7] = unitCode;
        data[8] = command.toByte();
        data[9] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

        return data;
    }

    @Override
    public String getDeviceId() {
        return sensorId + ID_DELIMITER + unitCode;
    }

    @Override
    public State convertToState(RFXComValueSelector valueSelector) throws RFXComException {

        State state = UnDefType.UNDEF;

        if (valueSelector.getItemClass() == NumberItem.class) {

            if (valueSelector == RFXComValueSelector.SIGNAL_LEVEL) {

                state = new DecimalType(signalLevel);

            } else if (valueSelector == RFXComValueSelector.BATTERY_LEVEL) {

                state = new DecimalType(batteryLevel);

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to NumberItem");
            }

        } else if (valueSelector.getItemClass() == RollershutterItem.class) {

            if (valueSelector == RFXComValueSelector.COMMAND) {

                switch (command) {
                    case CLOSE:
                        state = OpenClosedType.CLOSED;
                        break;

                    case OPEN:
                        state = OpenClosedType.OPEN;
                        break;

                    default:
                        break;
                }

            } else {
                throw new NumberFormatException("Can't convert " + valueSelector + " to RollershutterItem");
            }

        } else {

            throw new NumberFormatException("Can't convert " + valueSelector + " to " + valueSelector.getItemClass());

        }

        return state;
    }

    @Override
    public void setSubType(Object subType) throws RFXComException {
        this.subType = ((SubType) subType);
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {
        String[] ids = deviceId.split("\\" + ID_DELIMITER);
        if (ids.length != 2) {
            throw new RFXComException("Invalid device id '" + deviceId + "'");
        }

        sensorId = Integer.parseInt(ids[0]);
        unitCode = Byte.parseByte(ids[1]);
    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {

        switch (valueSelector) {
            case SHUTTER:
                if (type instanceof OpenClosedType) {
                    command = (type == OpenClosedType.CLOSED ? Commands.CLOSE : Commands.OPEN);
                } else if (type instanceof UpDownType) {
                    command = (type == UpDownType.UP ? Commands.OPEN : Commands.CLOSE);
                } else if (type instanceof StopMoveType) {
                    command = Commands.STOP;

                } else {
                    throw new NumberFormatException("Can't convert " + type + " to Command");
                }
                break;

            default:
                throw new RFXComException("Can't convert " + type + " to " + valueSelector);
        }
    }

    @Override
    public Object convertSubType(String subType) throws RFXComException {

        for (SubType s : SubType.values()) {
            if (s.toString().equals(subType)) {
                return s;
            }
        }

        // try to find sub type by number
        try {
            return SubType.values()[Integer.parseInt(subType)];
        } catch (Exception e) {
            throw new RFXComException("Unknown sub type " + subType);
        }
    }

    @Override
    public List<RFXComValueSelector> getSupportedInputValueSelectors() throws RFXComException {
        return supportedInputValueSelectors;
    }

    @Override
    public List<RFXComValueSelector> getSupportedOutputValueSelectors() throws RFXComException {
        return supportedOutputValueSelectors;
    }
}