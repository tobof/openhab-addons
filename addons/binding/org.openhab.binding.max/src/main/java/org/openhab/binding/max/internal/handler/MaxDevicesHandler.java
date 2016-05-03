/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.handler;

import static org.openhab.binding.max.MaxBinding.*;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.max.MaxBinding;
import org.openhab.binding.max.internal.command.Q_Command;
import org.openhab.binding.max.internal.command.S_ConfigCommand;
import org.openhab.binding.max.internal.command.S_ConfigCommand.ConfigCommandType;
import org.openhab.binding.max.internal.command.Z_Command;
import org.openhab.binding.max.internal.device.Device;
import org.openhab.binding.max.internal.device.DeviceType;
import org.openhab.binding.max.internal.device.EcoSwitch;
import org.openhab.binding.max.internal.device.HeatingThermostat;
import org.openhab.binding.max.internal.device.ShutterContact;
import org.openhab.binding.max.internal.device.ThermostatModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxDevicesHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MaxDevicesHandler extends BaseThingHandler implements DeviceStatusListener {

    private Logger logger = LoggerFactory.getLogger(MaxDevicesHandler.class);
    private MaxCubeBridgeHandler bridgeHandler;

    private String maxDeviceSerial;
    private boolean forceRefresh = true;
    private boolean propertiesSet = false;
    private boolean configSet = false;

    // actual refresh variables
    public static final int REFRESH_ACTUAL_MIN_RATE = 10; // minutes
    public static final int REFRESH_ACTUAL_DURATION = 120; // seconds
    private int refreshActualRate = 0;
    private boolean refreshingActuals = false;
    private ScheduledFuture<?> refreshActualsJob;
    private State originalSetTemp;
    private ThermostatModeType originalMode;
    private Runnable refreshActualsRestoreRunnable = new Runnable() {
        @Override
        public void run() {
            refreshActualsRestore();
        }
    };

    public MaxDevicesHandler(Thing thing) {
        super(thing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        try {
            Configuration config = getThing().getConfiguration();
            final String configDeviceId = (String) config.get(MaxBinding.PROPERTY_SERIAL_NUMBER);

            try {
                refreshActualRate = ((BigDecimal) config.get(MaxBinding.PROPERTY_REFRESH_ACTUAL_RATE)).intValueExact();
            } catch (Exception e) {
                refreshActualRate = 0;
            }

            if (configDeviceId != null) {
                maxDeviceSerial = configDeviceId;
            }
            if (maxDeviceSerial != null) {
                logger.debug("Initialized MAX! device handler for {}.", maxDeviceSerial);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Initialized MAX! device missing serialNumber configuration");
            }
            // until we get an update put the Thing offline
            updateStatus(ThingStatus.OFFLINE);
            propertiesSet = false;
            configSet = false;
            forceRefresh = true;
            getMaxCubeBridgeHandler();
        } catch (Exception e) {
            logger.debug("Exception occurred during initialize : {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#dispose()
     */
    @Override
    public void dispose() {
        logger.debug("Disposing MAX! device {} {}.", getThing().getUID(), maxDeviceSerial);
        if (refreshingActuals) {
            refreshActualsRestore();
        }
        if (refreshActualsJob != null && !refreshActualsJob.isCancelled()) {
            refreshActualsJob.cancel(true);
            refreshActualsJob = null;
        }
        if (bridgeHandler != null) {
            logger.trace("Clear MAX! device {} {} from bridge.", getThing().getUID(), maxDeviceSerial);
            bridgeHandler.clearDeviceList();
            bridgeHandler.unregisterDeviceStatusListener(this);
            bridgeHandler = null;
        }
        updateStatus(ThingStatus.OFFLINE);
        logger.debug("Disposed MAX! device {} {}.", getThing().getUID(), maxDeviceSerial);
        super.dispose();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.BaseThingHandler#thingUpdated
     * (org.eclipse.smarthome.core.thing.Thing)
     */
    @Override
    public void thingUpdated(Thing thing) {
        // TODO: test for changes in config and send update
        configSet = false;
        forceRefresh = true;
        super.thingUpdated(thing);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.debug("MAX! Device {}: Configuration update received", getThing().getThingTypeUID());

        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            logger.debug("MAX! Device {}: Configuration update {} to {}", getThing().getThingTypeUID(),
                    configurationParameter.getKey(), configurationParameter.getValue());
            if (configurationParameter.getKey().equals(PROPERTY_DEVICENAME)
                    || configurationParameter.getKey().equals(PROPERTY_ROOMID)) {
                updateDeviceName(configurationParameter);
            }
            if (configurationParameter.getKey().startsWith("action-")) {
                if (configurationParameter.getValue().toString().equals(BUTTON_ACTION_VALUE)) {
                    configurationParameter.setValue(BigDecimal.valueOf(BUTTON_NOACTION_VALUE));
                    if (configurationParameter.getKey().equals(ACTION_DEVICE_DELETE)) {
                        deviceDelete();
                    }
                }
            }
            configuration.put(configurationParameter.getKey(), configurationParameter.getValue());
        }

        // Persist changes and restart with new parameters
        updateConfiguration(configuration);
    }

    /**
     * sends the T command to the Cube to disassociate the device from the MAX! Cube.
     */
    private void deviceDelete() {
        MaxCubeBridgeHandler maxCubeBridge = getMaxCubeBridgeHandler();
        if (maxCubeBridge != null) {
            maxCubeBridge.sendDeviceDelete(maxDeviceSerial);
            dispose();
        }
    }

    /**
     * @param configurationParameter
     */
    private void updateDeviceName(Entry<String, Object> configurationParameter) {
        MaxCubeBridgeHandler maxCubeBridge = getMaxCubeBridgeHandler();
        if (maxCubeBridge != null) {
            Device device = getMaxCubeBridgeHandler().getDevice(maxDeviceSerial);
            String name = configurationParameter.getValue().toString();
            if (device != null && configurationParameter.getKey().equals(PROPERTY_DEVICENAME)
                    && !(name.equals(device.getName()))) {
                logger.info("Updating device name for {} to {}", getThing().getUID().getAsString(), name);
                device.setName(name);
                getMaxCubeBridgeHandler().sendDeviceAndRoomNameUpdate(name);
                SendCommand sendCommand = new SendCommand(maxDeviceSerial, new Q_Command(), "Reload Data");
                maxCubeBridge.queueCommand(sendCommand);
            }

            if (device != null && configurationParameter.getKey().equals(PROPERTY_ROOMID)) {
                int roomId = ((BigDecimal) configurationParameter.getValue()).intValue();
                if (roomId != device.getRoomId()) {
                    logger.info("Updating room for {} to {}", getThing().getUID().getAsString(), roomId);
                    device.setRoomId(roomId);
                    //TODO: handle if a room has no more devices, probably should be deleted
                    getMaxCubeBridgeHandler().sendDeviceAndRoomNameUpdate(name);
                    SendCommand sendCommand = new SendCommand(maxDeviceSerial,
                            Z_Command.wakeupDevice(device.getRFAddress()),
                            "WakeUp device" + getThing().getUID().getAsString());
                    maxCubeBridge.queueCommand(sendCommand);
                    sendCommand = new SendCommand(maxDeviceSerial,
                            new S_ConfigCommand(device.getRFAddress(), roomId, ConfigCommandType.SetRoom), "Set Room");
                    maxCubeBridge.queueCommand(sendCommand);

                    sendCommand = new SendCommand(maxDeviceSerial, new Q_Command(), "Reload Data");
                    maxCubeBridge.queueCommand(sendCommand);
                }
            }
        } else {
            logger.warn("MAX! Cube LAN gateway bridge handler not found. Cannot handle update without bridge.");
        }
    }

    private synchronized MaxCubeBridgeHandler getMaxCubeBridgeHandler() {

        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.debug("Required bridge not defined for device {}.", maxDeviceSerial);
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof MaxCubeBridgeHandler) {
                this.bridgeHandler = (MaxCubeBridgeHandler) handler;
                this.bridgeHandler.registerDeviceStatusListener(this);
                forceRefresh = true;
            } else {
                logger.debug("No available bridge handler found for {} bridge {} .", maxDeviceSerial, bridge.getUID());
                return null;
            }
        }
        return this.bridgeHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        MaxCubeBridgeHandler maxCubeBridge = getMaxCubeBridgeHandler();
        if (maxCubeBridge == null) {
            logger.warn("MAX! Cube LAN gateway bridge handler not found. Cannot handle command without bridge.");
            return;
        }
        if (command instanceof RefreshType) {
            forceRefresh = true;
            maxCubeBridge.handleCommand(channelUID, command);
            return;
        }
        if (maxDeviceSerial == null) {
            logger.warn("Serial number missing. Can't send command to device '{}'", getThing());
            return;
        }

        if (channelUID.getId().equals(CHANNEL_SETTEMP) || channelUID.getId().equals(CHANNEL_MODE)) {
            if (refreshingActuals) {
                refreshActualsRestore();
            }
            SendCommand sendCommand = new SendCommand(maxDeviceSerial, channelUID, command);
            maxCubeBridge.queueCommand(sendCommand);
        } else {
            logger.warn("Setting of channel {} not possible. Read-only", channelUID);
        }
    }

    @Override
    public void onDeviceStateChanged(ThingUID bridge, Device device) {
        if (device.getSerialNumber().equals(maxDeviceSerial)) {
            if (!device.isLinkStatusError()) {
                if (!refreshingActuals) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Updating Actual Temperature");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
            }
            if (!propertiesSet) {
                setProperties(device);
            }
            if (!configSet) {
                setDeviceConfiguration(device);
            }
            if (refreshActualRate >= REFRESH_ACTUAL_MIN_RATE && (device.getType() == DeviceType.HeatingThermostat
                    || device.getType() == DeviceType.HeatingThermostatPlus)) {
                refreshActualCheck((HeatingThermostat) device);
            }
            if (device.isUpdated() || forceRefresh) {
                logger.debug("Updating states of {} {} ({}) id: {}", device.getType(), device.getName(),
                        device.getSerialNumber(), getThing().getUID());
                switch (device.getType()) {
                    case WallMountedThermostat:
                    case HeatingThermostat:
                    case HeatingThermostatPlus:
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_LOCKED),
                                ((HeatingThermostat) device).isPanelLocked() ? OpenClosedType.CLOSED
                                        : OpenClosedType.OPEN);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_SETTEMP),
                                ((HeatingThermostat) device).getTemperatureSetpoint());
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_MODE),
                                ((HeatingThermostat) device).getModeString());
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERY),
                                ((HeatingThermostat) device).getBatteryLow());
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE),
                                ((HeatingThermostat) device).getValvePosition());
                        State actualTemp = ((HeatingThermostat) device).getTemperatureActual();
                        if (actualTemp != DecimalType.ZERO) {
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ACTUALTEMP), actualTemp);
                        }
                        break;
                    case ShutterContact:
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_CONTACT_STATE),
                                ((ShutterContact) device).getShutterState());
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERY),
                                ((ShutterContact) device).getBatteryLow());
                        break;
                    case EcoSwitch:
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERY),
                                ((EcoSwitch) device).getBatteryLow());
                        break;
                    default:
                        logger.debug("Unhandled Device {}.", device.getType());
                        break;
                }
                forceRefresh = false;
                device.setUpdated(false);
            } else {
                logger.debug("No changes for {} {} ({}) id: {}", device.getType(), device.getName(),
                        device.getSerialNumber(), getThing().getUID());
            }
        }
    }

    private void refreshActualCheck(HeatingThermostat device) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        if (device.getActualTempLastUpdated() == null) {
            Calendar t = Calendar.getInstance();
            t.add(Calendar.MINUTE, REFRESH_ACTUAL_MIN_RATE * -1);
            device.setActualTempLastUpdated(t.getTime());
            logger.info("Actual date reset for {} {} ({}) id: {}", device.getType(), device.getName(),
                    device.getSerialNumber(), getThing().getUID());
        }
        long timediff = Calendar.getInstance().getTime().getTime() - device.getActualTempLastUpdated().getTime();
        if (timediff > (refreshActualRate * 1000 * 60)) {
            if (!refreshingActuals) {

                logger.debug("Actual needs updating for {} {} ({}) id: {}", device.getType(), device.getName(),
                        device.getSerialNumber(), getThing().getUID());

                originalSetTemp = device.getTemperatureSetpoint();
                originalMode = device.getMode();

                if (originalMode == ThermostatModeType.MANUAL || originalMode == ThermostatModeType.AUTOMATIC) {
                    BigDecimal temporaryTemp = ((DecimalType) originalSetTemp).toBigDecimal()
                            .add(BigDecimal.valueOf(0.5));
                    logger.debug("Actuals Refresh: Setting Temp {}", temporaryTemp);
                    handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_SETTEMP), new DecimalType(temporaryTemp));
                    refreshingActuals = true;
                } else {
                    logger.debug("Defer Actuals refresh. Only manual refresh for mode AUTOMATIC & MANUAL");
                    device.setActualTempLastUpdated(Calendar.getInstance().getTime());
                }

                if (refreshingActuals) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Updating Actual Temperature");

                    if (refreshActualsJob == null || refreshActualsJob.isCancelled()) {
                        refreshActualsJob = scheduler.schedule(refreshActualsRestoreRunnable, REFRESH_ACTUAL_DURATION,
                                TimeUnit.SECONDS);
                    }

                    device.setActualTempLastUpdated(Calendar.getInstance().getTime());
                }
            }
            logger.debug("Actual Refresh in progress for {} {} ({}) id: {}", device.getType(), device.getName(),
                    device.getSerialNumber(), getThing().getUID());
        } else {
            logger.trace("Actual date for {} {} ({}) : {}", device.getType(), device.getName(),
                    device.getSerialNumber(), dateFormat.format(device.getActualTempLastUpdated().getTime()));
        }

    }

    /**
     * Send the commands to restore the original settings for mode & temperature
     * to end the automatic update cycle
     */
    private synchronized void refreshActualsRestore() {
        try {
            refreshingActuals = false;
            if (originalMode == ThermostatModeType.AUTOMATIC || originalMode == ThermostatModeType.MANUAL) {
                logger.debug("Finished Actuals Refresh: Restoring Temp {}", originalSetTemp);
                handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_SETTEMP), (Command) originalSetTemp);
            }

            if (refreshActualsJob != null && !refreshActualsJob.isCancelled()) {
                refreshActualsJob.cancel(true);
                refreshActualsJob = null;
            }
        } catch (Exception e) {
            logger.debug("Exception occurred during Actuals Refresh : {}", e.getMessage(), e);
        }
    }

    @Override
    public void onDeviceRemoved(MaxCubeBridgeHandler bridge, Device device) {
        if (device.getSerialNumber().equals(maxDeviceSerial)) {
            bridgeHandler.unregisterDeviceStatusListener(this);
            bridgeHandler = null;
            forceRefresh = true;
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void onDeviceAdded(Bridge bridge, Device device) {
        forceRefresh = true;
    }

    /**
     * Set the forceRefresh flag to ensure update when next data is coming
     */
    public void setForceRefresh() {
        forceRefresh = true;
    }

    /**
     * Set the properties for this device
     *
     * @param device
     */
    private void setProperties(Device device) {
        try {
            logger.debug("MAX! {} {} properties update", device.getType().toString(), device.getSerialNumber());
            Map<String, String> properties = editProperties();
            properties.put(Thing.PROPERTY_MODEL_ID, device.getType().toString());
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, device.getSerialNumber());
            properties.put(Thing.PROPERTY_VENDOR, MaxBinding.PROPERTY_VENDOR_NAME);
            updateProperties(properties);
            // TODO: Remove this once UI is displaying this info
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                logger.debug("key: {}  : {}", entry.getKey(), entry.getValue());
            }
            logger.debug("properties updated");
            propertiesSet = true;
        } catch (Exception e) {
            logger.debug("Exception occurred during property edit: {}", e.getMessage(), e);
        }
    }

    /**
     * Set the Configurable properties for this device
     *
     * @param device
     */

    private void setDeviceConfiguration(Device device) {
        try {
            logger.debug("MAX! {} {} configuration update", device.getType().toString(), device.getSerialNumber());
            Configuration configuration = editConfiguration();
            configuration.put(MaxBinding.PROPERTY_ROOMNAME, device.getRoomName());
            configuration.put(MaxBinding.PROPERTY_ROOMID, new BigDecimal(device.getRoomId()));
            configuration.put(MaxBinding.PROPERTY_DEVICENAME, device.getName());
            configuration.put(MaxBinding.PROPERTY_RFADDRESS, device.getRFAddress());
            updateConfiguration(configuration);
            logger.debug("Config updated: {}", configuration.getProperties());
            configSet = true;
        } catch (Exception e) {
            logger.debug("Exception occurred during configuration edit: {}", e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#
     * bridgeHandlerInitialized
     * (org.eclipse.smarthome.core.thing.binding.ThingHandler,
     * org.eclipse.smarthome.core.thing.Bridge)
     */
    @Override
    public void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        logger.debug("Bridge {} initialized for device: {}", bridge.getUID().toString(),
                getThing().getUID().toString());
        if (bridgeHandler != null) {
            bridgeHandler.unregisterDeviceStatusListener(this);
            bridgeHandler = null;
        }
        this.bridgeHandler = (MaxCubeBridgeHandler) thingHandler;
        this.bridgeHandler.registerDeviceStatusListener(this);
        forceRefresh = true;
        super.bridgeHandlerInitialized(thingHandler, bridge);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#
     * bridgeHandlerDisposed
     * (org.eclipse.smarthome.core.thing.binding.ThingHandler,
     * org.eclipse.smarthome.core.thing.Bridge)
     */
    @Override
    public void bridgeHandlerDisposed(ThingHandler thingHandler, Bridge bridge) {
        logger.debug("Bridge {} disposed for device: {}", bridge.getUID().toString(), getThing().getUID().toString());
        bridgeHandler = null;
        forceRefresh = true;
        super.bridgeHandlerDisposed(thingHandler, bridge);
    }

}
