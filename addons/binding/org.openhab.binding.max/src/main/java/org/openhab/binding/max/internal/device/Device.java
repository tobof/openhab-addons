/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.device;

import java.util.Date;
import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.openhab.binding.max.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for devices provided by the MAX! protocol.
 *
 * @author Andreas Heil (info@aheil.de)
 * @author Marcel Verpaalen - OH2 update
 * @since 1.4.0
 */
public abstract class Device {

    private final static Logger logger = LoggerFactory.getLogger(Device.class);

    private String serialNumber = "";
    private String rfAddress = "";
    private int roomId = -1;
    private DeviceConfiguration config;

    private boolean updated;
    private boolean batteryLow;

    private boolean initialized;
    private boolean answer;
    private boolean error;
    private boolean valid;
    private boolean dstSettingsActive;
    private boolean gatewayKnown;
    private boolean panelLocked;
    private boolean linkStatusError;

    public Device(DeviceConfiguration c) {
        this.serialNumber = c.getSerialNumber();
        this.rfAddress = c.getRFAddress();
        this.roomId = c.getRoomId();
        this.config = c;
    }

    public abstract DeviceType getType();

    public String getName() {
        String deviceName = "";
        if (config.getName() != null) {
            deviceName = config.getName();
        }
        return deviceName;
    }

    private static Device create(String rfAddress, List<DeviceConfiguration> configurations) {
        Device returnValue = null;
        for (DeviceConfiguration c : configurations) {
            if (c.getRFAddress().toUpperCase().equals(rfAddress.toUpperCase())) {
                switch (c.getDeviceType()) {
                    case HeatingThermostatPlus:
                    case HeatingThermostat:
                        HeatingThermostat thermostat = new HeatingThermostat(c);
                        thermostat.setType(c.getDeviceType());
                        return thermostat;
                    case EcoSwitch:
                        return new EcoSwitch(c);
                    case ShutterContact:
                        return new ShutterContact(c);
                    case WallMountedThermostat:
                        return new WallMountedThermostat(c);
                    default:
                        return new UnsupportedDevice(c);
                }
            }
        }
        return returnValue;
    }

    public static Device create(byte[] raw, List<DeviceConfiguration> configurations) {
        if (raw.length == 0) {
            return null;
        }

        String rfAddress = Utils.toHex(raw[0] & 0xFF, raw[1] & 0xFF, raw[2] & 0xFF);

        // Based on the RF address and the corresponding configuration,
        // create the device based on the type specified in it's configuration

        Device device = Device.create(rfAddress, configurations);
        if (device == null) {
            logger.warn("Can't create device from received message, returning NULL.");
            return null;
        }

        return Device.update(raw, configurations, device);
    }

    public static Device update(byte[] raw, List<DeviceConfiguration> configurations, Device device) {

        String rfAddress = device.getRFAddress();

        // byte 4 is skipped

        // multiple device information are encoded in those particular bytes
        boolean[] bits1 = Utils.getBits(Utils.fromByte(raw[4]));
        boolean[] bits2 = Utils.getBits(Utils.fromByte(raw[5]));

        device.setInitialized(bits1[1]);
        device.setAnswer(bits1[2]);
        device.setError(bits1[3]);
        device.setValid(bits1[4]);

        device.setDstSettingActive(bits2[3]);
        device.setGatewayKnown(bits2[4]);
        device.setPanelLocked(bits2[5]);
        device.setLinkStatusError(bits2[6]);
        device.setBatteryLow(bits2[7]);

        logger.trace("Device {} ({}): L Message length: {} content: {}", rfAddress, device.getType().toString(),
                raw.length, Utils.getHex(raw));

        // TODO move the device specific readings into the sub classes
        switch (device.getType()) {
            case WallMountedThermostat:
            case HeatingThermostat:
            case HeatingThermostatPlus:
                HeatingThermostat heatingThermostat = (HeatingThermostat) device;
                // "xxxx xx00 = automatic, xxxx xx01 = manual, xxxx xx10 = vacation, xxxx xx11 = boost":
                if (bits2[1] == false && bits2[0] == false) {
                    heatingThermostat.setMode(ThermostatModeType.AUTOMATIC);
                } else if (bits2[1] == false && bits2[0] == true) {
                    heatingThermostat.setMode(ThermostatModeType.MANUAL);
                } else if (bits2[1] == true && bits2[0] == false) {
                    heatingThermostat.setMode(ThermostatModeType.VACATION);
                } else if (bits2[1] == true && bits2[0] == true) {
                    heatingThermostat.setMode(ThermostatModeType.BOOST);
                } else {
                    logger.debug("Device {} ({}): Unknown mode", rfAddress, device.getType().toString());
                }

                heatingThermostat.setValvePosition(raw[6] & 0xFF);
                heatingThermostat.setTemperatureSetpoint(raw[7] & 0x7F);

                // 9 2 858B Date until (05-09-2011) (see Encoding/Decoding
                // date/time)
                // B 1 2E Time until (23:00) (see Encoding/Decoding date/time)
                String hexDate = Utils.toHex(raw[8] & 0xFF, raw[9] & 0xFF);
                int dateValue = Utils.fromHex(hexDate);
                int timeValue = raw[10] & 0xFF;
                Date date = Utils.resolveDateTime(dateValue, timeValue);
                heatingThermostat.setDateSetpoint(date);

                int actualTemp = 0;
                if (device.getType() == DeviceType.WallMountedThermostat) {
                    actualTemp = (raw[11] & 0xFF) + (raw[7] & 0x80) * 2;

                } else {
                    if (heatingThermostat.getMode() != ThermostatModeType.VACATION
                            && heatingThermostat.getMode() != ThermostatModeType.BOOST) {
                        actualTemp = (raw[8] & 0xFF) * 256 + (raw[9] & 0xFF);
                    } else {
                        logger.debug("Device {} ({}): No temperature reading in {} mode", rfAddress,
                                device.getType().toString(), heatingThermostat.getMode());
                    }
                }
                logger.debug("Device {} ({}): Actual Temperature : {}", rfAddress, device.getType().toString(),
                        (double) actualTemp / 10);
                heatingThermostat.setTemperatureActual((double) actualTemp / 10);
                break;
            case EcoSwitch:
                String eCoSwitchData = Utils.toHex(raw[3] & 0xFF, raw[4] & 0xFF, raw[5] & 0xFF);
                logger.trace("Device {} ({}): Status bytes : {}", rfAddress, device.getType().toString(),
                        eCoSwitchData);
                EcoSwitch ecoswitch = (EcoSwitch) device;
                // xxxx xx10 = shutter open, xxxx xx00 = shutter closed
                if (bits2[1] == true && bits2[0] == false) {
                    ecoswitch.setEcoMode(OnOffType.ON);
                    logger.trace("Device {} ({}): status: ON", rfAddress, device.getType().toString());
                } else if (bits2[1] == false && bits2[0] == false) {
                    ecoswitch.setEcoMode(OnOffType.OFF);
                    logger.trace("Device {} ({}): Status: OFF", rfAddress, device.getType().toString());
                } else {
                    logger.trace("Device {} ({}): Status switch status Unknown (true-true)", rfAddress,
                            device.getType().toString());
                }
                break;
            case ShutterContact:
                ShutterContact shutterContact = (ShutterContact) device;
                // xxxx xx10 = shutter open, xxxx xx00 = shutter closed
                if (bits2[1] == true && bits2[0] == false) {
                    shutterContact.setShutterState(OpenClosedType.OPEN);
                    logger.debug("Device {} ({}): Status: Open", rfAddress, device.getType().toString());
                } else if (bits2[1] == false && bits2[0] == false) {
                    shutterContact.setShutterState(OpenClosedType.CLOSED);
                    logger.debug("Device {} ({}): Status: Closed", rfAddress, device.getType().toString());
                } else {
                    logger.trace("Device {} ({}): Status switch status Unknown (true-true)", rfAddress,
                            device.getType().toString());
                }

                break;
            default:
                logger.debug("Unhandled Device. DataBytes: " + Utils.getHex(raw));
                break;

        }
        return device;
    }

    private final void setBatteryLow(boolean batteryLow) {
        if (this.batteryLow != batteryLow) {
            this.updated = true;
        }
        this.batteryLow = batteryLow;
    }

    public final OnOffType getBatteryLow() {
        return (this.batteryLow ? OnOffType.ON : OnOffType.OFF);
    }

    public final String getRFAddress() {
        return this.rfAddress;
    }

    public final void setRFAddress(String rfAddress) {
        this.rfAddress = rfAddress;
    }

    public final int getRoomId() {
        return roomId;
    }

    public final void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public final String getRoomName() {
        String roomName = "";
        if (config.getRoomName() != null) {
            roomName = config.getRoomName();
        }
        return roomName;
    }

    private void setLinkStatusError(boolean linkStatusError) {
        if (this.linkStatusError != linkStatusError) {
            this.updated = true;
        }
        this.linkStatusError = linkStatusError;
    }

    private void setPanelLocked(boolean panelLocked) {
        if (this.panelLocked != panelLocked) {
            this.updated = true;
        }
        this.panelLocked = panelLocked;
    }

    private void setGatewayKnown(boolean gatewayKnown) {
        if (this.gatewayKnown != gatewayKnown) {
            this.updated = true;
        }
        this.gatewayKnown = gatewayKnown;
    }

    private void setDstSettingActive(boolean dstSettingsActive) {
        if (this.dstSettingsActive != dstSettingsActive) {
            this.updated = true;
        }
        this.dstSettingsActive = dstSettingsActive;
    }

    public boolean isDstSettingsActive() {
        return dstSettingsActive;
    }

    private void setValid(boolean valid) {
        if (this.valid != valid) {
            this.updated = true;
        }
        this.valid = valid;
    }

    private void setError(boolean error) {
        if (this.error != error) {
            this.updated = true;
        }
        this.error = error;

    }

    public String getSerialNumber() {
        return serialNumber;
    }

    private void setInitialized(boolean initialized) {
        if (this.initialized != initialized) {
            this.updated = true;
        }
        this.initialized = initialized;
    }

    private void setAnswer(boolean answer) {
        if (this.answer != answer) {
            this.updated = true;
        }
        this.answer = answer;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isAnswer() {
        return answer;
    }

    public boolean isError() {
        return error;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isGatewayKnown() {
        return gatewayKnown;
    }

    public boolean isPanelLocked() {
        return panelLocked;
    }

    public boolean isLinkStatusError() {
        return linkStatusError;
    }

}
