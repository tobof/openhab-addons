/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import org.eclipse.smarthome.core.library.items.DateTimeItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

import java.util.Arrays;
import java.util.List;

/**
 * RFXCOM data class for Date and Time message.
 *
 * @author Damien Servant
 * @since 1.9.0
 */
public class RFXComDateTimeMessage extends RFXComBaseMessage {

    public enum SubType {
        RTGR328N(1),

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

        public static SubType fromByte(int input) {
            for (SubType c : SubType.values()) {
                if (c.subType == input) {
                    return c;
                }
            }

            return SubType.UNKNOWN;
        }
    }

    private final static List<RFXComValueSelector> supportedInputValueSelectors = Arrays.asList(
            RFXComValueSelector.SIGNAL_LEVEL, RFXComValueSelector.BATTERY_LEVEL, RFXComValueSelector.DATE_TIME);

    private final static List<RFXComValueSelector> supportedOutputValueSelectors = Arrays.asList();

    public SubType subType = SubType.UNKNOWN;
    public int sensorId = 0;
    String dateTime = "yyyy-MM-dd'T'HH:mm:ss";
    int yy = 0;
    int MM = 0;
    int dd = 0;
    int dow = 0;
    int HH = 0;
    int mm = 0;
    int ss = 0;

    public byte signalLevel = 0;
    public byte batteryLevel = 0;

    public RFXComDateTimeMessage() {
        packetType = PacketType.DATE_TIME;
    }

    public RFXComDateTimeMessage(byte[] data) {
        encodeMessage(data);
    }

    @Override
    public String toString() {
        String str = "";

        str += super.toString();
        str += ", Sub type = " + subType;
        str += ", Id = " + sensorId;
        str += ", Date Time = " + dateTime;
        str += ", Signal level = " + signalLevel;
        str += ", Battery level = " + batteryLevel;

        return str;
    }

    @Override
    public void encodeMessage(byte[] data) {

        super.encodeMessage(data);

        subType = SubType.fromByte(super.subType);

        sensorId = (data[4] & 0xFF) << 8 | (data[5] & 0xFF);

        yy = data[6] & 0xFF;
        MM = data[7] & 0xFF;
        dd = data[8] & 0xFF;
        dow = data[9] & 0xFF;
        HH = data[10] & 0xFF;
        mm = data[11] & 0xFF;
        ss = data[12] & 0xFF;

        dateTime = String.format("20%02d-%02d-%02dT%02d:%02d:%02d", yy, MM, dd, HH, mm, ss);

        signalLevel = (byte) ((data[13] & 0xF0) >> 4);
        batteryLevel = (byte) (data[13] & 0x0F);
    }

    @Override
    public byte[] decodeMessage() {
        byte[] data = new byte[14];

        data[0] = (byte) (data.length - 1);
        data[1] = RFXComBaseMessage.PacketType.DATE_TIME.toByte();
        data[2] = subType.toByte();
        data[3] = seqNbr;
        data[4] = (byte) ((sensorId & 0xFF00) >> 8);
        data[5] = (byte) (sensorId & 0x00FF);
        data[6] = (byte) (yy & 0x00FF);
        data[7] = (byte) (MM & 0x00FF);
        data[8] = (byte) (dd & 0x00FF);
        data[9] = (byte) (dow & 0x00FF);
        data[10] = (byte) (HH & 0x00FF);
        data[11] = (byte) (mm & 0x00FF);
        data[12] = (byte) (ss & 0x00FF);
        data[13] = (byte) (((signalLevel & 0x0F) << 4) | (batteryLevel & 0x0F));

        return data;
    }

    @Override
    public State convertToState(RFXComValueSelector valueSelector) throws RFXComException {
        State state;

        if (valueSelector.getItemClass() == NumberItem.class) {

            if (valueSelector == RFXComValueSelector.SIGNAL_LEVEL) {

                state = new DecimalType(signalLevel);

            } else if (valueSelector == RFXComValueSelector.BATTERY_LEVEL) {

                state = new DecimalType(batteryLevel);

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to NumberItem");
            }

        } else if (valueSelector.getItemClass() == DateTimeItem.class) {

            if (valueSelector == RFXComValueSelector.DATE_TIME) {

                state = new DateTimeType(dateTime);

            } else {
                throw new RFXComException("Can't convert " + valueSelector + " to StringItem");
            }

        } else {

            throw new RFXComException("Can't convert " + valueSelector + " to " + valueSelector.getItemClass());

        }

        return state;
    }

    @Override
    public void convertFromState(RFXComValueSelector valueSelector, Type type) throws RFXComException {
        throw new RFXComException("Not supported");
    }

    @Override
    public String getDeviceId() {
        return String.valueOf(sensorId);
    }

    @Override
    public void setDeviceId(String deviceId) throws RFXComException {
        throw new RFXComException("Not supported");
    }

    @Override
    public List<RFXComValueSelector> getSupportedInputValueSelectors() throws RFXComException {
        return supportedInputValueSelectors;
    }

    @Override
    public List<RFXComValueSelector> getSupportedOutputValueSelectors() throws RFXComException {
        return supportedOutputValueSelectors;
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
            return RFXComBlinds1Message.SubType.fromByte(Integer.parseInt(subType));
        } catch (NumberFormatException e) {
            throw new RFXComException("Unknown sub type " + subType);
        }
    }

    @Override
    public void setSubType(Object subType) throws RFXComException {
        this.subType = (SubType) subType;
    }
}