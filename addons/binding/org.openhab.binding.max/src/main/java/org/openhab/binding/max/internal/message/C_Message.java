/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.message;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.net.util.Base64;
import org.openhab.binding.max.internal.Utils;
import org.openhab.binding.max.internal.device.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * The C message contains configuration about a MAX! device.
 *
 * @author Andreas Heil (info@aheil.de)
 * @author Marcel Verpaalen - Detailed parsing, OH2 Update
 * @since 1.4.0
 */
public final class C_Message extends Message {

    private static final Logger logger = LoggerFactory.getLogger(C_Message.class);

    private String rfAddress = null;
    private int length = 0;
    private DeviceType deviceType = null;
    private int roomId = -1;
    private String serialNumber = null;
    private String tempComfort = null;
    private String tempEco = null;
    private String tempSetpointMax = null;
    private String tempSetpointMin = null;
    private String tempOffset = null;
    private String tempOpenWindow = null;
    private String durationOpenWindow = null;
    private String decalcification = null;
    private String valveMaximum = null;
    private String valveOffset = null;
    private String programData = null;
    private String boostDuration = null;
    private String boostValve = null;
    private Map<String, Object> properties = new HashMap<>();

    public C_Message(String raw) {
        super(raw);
        String[] tokens = this.getPayload().split(Message.DELIMETER);

        rfAddress = tokens[0];

        byte[] bytes = Base64.decodeBase64(tokens[1].getBytes());

        int[] data = new int[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            data[i] = bytes[i] & 0xFF;
        }

        length = data[0];
        if (length != data.length - 1) {
            logger.debug("C_Message malformed: wrong data length. Expected bytes {}, actual bytes {}", length,
                    data.length - 1);
        }

        String rfAddress2 = Utils.toHex(data[1], data[2], data[3]);
        if (!rfAddress.toUpperCase().equals(rfAddress2.toUpperCase())) {
            logger.debug("C_Message malformed: wrong RF address. Expected address {}, actual address {}",
                    rfAddress.toUpperCase(), rfAddress2.toUpperCase());
        }

        deviceType = DeviceType.create(data[4]);
        roomId = data[5] & 0xFF;

        serialNumber = getSerialNumber(bytes);
        if (deviceType == DeviceType.HeatingThermostatPlus || deviceType == DeviceType.HeatingThermostat
                || deviceType == DeviceType.WallMountedThermostat) {
            parseHeatingThermostatData(bytes);
        }
        if (deviceType == DeviceType.EcoSwitch || deviceType == DeviceType.ShutterContact) {
            logger.trace("Device {} type {} Data:", rfAddress, deviceType.toString(), parseData(bytes));
        }
    }

    private String getSerialNumber(byte[] bytes) {
        byte[] sn = new byte[10];

        for (int i = 0; i < 10; i++) {
            sn[i] = bytes[i + 8];
        }

        try {
            return new String(sn, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.debug("Cannot encode serial number from C message due to encoding issues.");
        }

        return "";
    }

    private String parseData(byte[] bytes) {
        if (bytes.length <= 18) {
            return "";
        }
        try {
            int DataStart = 18;
            byte[] sn = new byte[bytes.length - DataStart];

            for (int i = 0; i < sn.length; i++) {
                sn[i] = bytes[i + DataStart];
            }
            logger.trace("DataBytes: " + Utils.getHex(sn));
            try {
                return new String(sn, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.debug("Cannot encode device string from C message due to encoding issues.");
            }

        } catch (Exception e) {
            logger.debug("Exception occurred during parsing: {}", e.getMessage(), e);
        }

        return "";
    }

    private void parseHeatingThermostatData(byte[] bytes) {
        try {

            int plusDataStart = 18;
            int programDataStart = 11;
            tempComfort = Float.toString(bytes[plusDataStart] / 2);
            tempEco = Float.toString(bytes[plusDataStart + 1] / 2);
            tempSetpointMax = Float.toString(bytes[plusDataStart + 2] / 2);
            tempSetpointMin = Float.toString(bytes[plusDataStart + 3] / 2);
            properties.put("Temp Comfort", tempComfort);
            properties.put("Temp Eco", tempEco);
            properties.put("Temp Setpoint Max", tempSetpointMax);
            properties.put("Temp Setpoint Min", tempSetpointMin);
            if (bytes.length < 211) {
                // Device is a WallMountedThermostat
                programDataStart = 4;
                logger.trace("WallThermostat byte {}: {}", bytes.length - 3,
                        Float.toString(bytes[bytes.length - 3] & 0xFF));
                logger.trace("WallThermostat byte {}: {}", bytes.length - 2,
                        Float.toString(bytes[bytes.length - 2] & 0xFF));
                logger.trace("WallThermostat byte {}: {}", bytes.length - 1,
                        Float.toString(bytes[bytes.length - 1] & 0xFF));
            } else {
                // Device is a HeatingThermostat(+)
                tempOffset = Double.toString((bytes[plusDataStart + 4] / 2) - 3.5);
                tempOpenWindow = Float.toString(bytes[plusDataStart + 5] / 2);
                durationOpenWindow = Float.toString(bytes[plusDataStart + 6]);
                boostDuration = Float.toString(bytes[plusDataStart + 7] & 0xFF >> 5);
                boostValve = Float.toString((bytes[plusDataStart + 7] & 0x1F) * 5);
                decalcification = Float.toString(bytes[plusDataStart + 8]);
                valveMaximum = Float.toString(bytes[plusDataStart + 9] & 0xFF * 100 / 255);
                valveOffset = Float.toString(bytes[plusDataStart + 10] & 0xFF * 100 / 255);
                properties.put("Temp Offset", tempOffset);
                properties.put("Temp Open Window", tempOpenWindow);
                properties.put("Duration Open Windoww", durationOpenWindow);
                properties.put("Duration Boost", boostDuration);
                properties.put("Duration Boost", boostValve);
                properties.put("Decalcification", decalcification);
                properties.put("ValveMaximum", valveMaximum);
                properties.put("ValveOffset", valveOffset);
            }
            programData = "";
            int ln = 13 * 6; // first day = Sat
            String startTime = "00:00h";
            for (int char_idx = plusDataStart + programDataStart; char_idx < (plusDataStart + programDataStart
                    + 26 * 7); char_idx++) {
                if (ln % 13 == 0) {
                    programData += "\r\n Day " + Integer.toString((ln / 13) % 7) + ": ";
                    startTime = "00:00h";
                }
                int progTime = (bytes[char_idx + 1] & 0xFF) * 5 + (bytes[char_idx] & 0x01) * 1280;
                int progMinutes = progTime % 60;
                int progHours = (progTime - progMinutes) / 60;
                String endTime = Integer.toString(progHours) + ":" + String.format("%02d", progMinutes) + "h";
                programData += startTime + "-" + endTime + " " + Double.toString(bytes[char_idx] / 4) + "C  ";
                startTime = endTime;
                char_idx++;
                ln++;
            }

        } catch (Exception e) {
            logger.debug("Exception occurred during heater data: {}", e.getMessage(), e);
        }
        return;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public MessageType getType() {
        return MessageType.C;
    }

    public String getRFAddress() {
        return rfAddress;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public int getRoomID() {
        return roomId;
    }

    @Override
    public void debug(Logger logger) {
        logger.debug("=== C_Message === ");
        logger.trace("\tRAW:                    {}", this.getPayload());
        logger.debug("DeviceType:               {}", deviceType.toString());
        logger.debug("SerialNumber:             {}", serialNumber);
        logger.debug("RFAddress:                {}", rfAddress);
        logger.debug("RoomID:                   {}", roomId);
        for (String key : properties.keySet()) {
            logger.debug("{}:{}{}", key, Strings.repeat(" ", 25 - key.length()), properties.get(key));
        }
        logger.trace("ProgramData:          {}", programData);
    }
}
