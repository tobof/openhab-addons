/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zway.handler;

import static org.openhab.binding.zway.ZWayBindingConstants.THING_TYPE_DEVICE;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.zway.config.ZWayZWaveDeviceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fh_zwickau.informatik.sensor.model.devices.Device;
import de.fh_zwickau.informatik.sensor.model.devices.DeviceList;
import de.fh_zwickau.informatik.sensor.model.zwaveapi.devices.ZWaveDevice;

/**
 * The {@link ZWayZWaveDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Patrick Hecker - Initial contribution
 */
public class ZWayZWaveDeviceHandler extends ZWayDeviceHandler {
    public final static ThingTypeUID SUPPORTED_THING_TYPE = THING_TYPE_DEVICE;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ZWayZWaveDeviceConfiguration mConfig = null;

    private class Initializer implements Runnable {

        @Override
        public void run() {
            ZWayBridgeHandler zwayBridgeHandler = getZWayBridgeHandler();
            if (zwayBridgeHandler != null && zwayBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                ThingStatusInfo statusInfo = zwayBridgeHandler.getThing().getStatusInfo();
                logger.debug("Change Z-Way Z-Wave device status to bridge status: {}", statusInfo);

                // Set thing status to bridge status
                updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());

                // Add all available channels
                logger.debug("Add all available channels");
                DeviceList deviceList = getZWayBridgeHandler().getZWayApi().getDevices();
                if (deviceList != null) {
                    logger.debug("Z-Way devices loaded ({} physical devices)",
                            deviceList.getDevicesGroupByNodeId().size());

                    // https://community.openhab.org/t/oh2-major-bug-with-scheduled-jobs/12350/11
                    // If any execution of the task encounters an exception, subsequent executions are
                    // suppressed. Otherwise, the task will only terminate via cancellation or
                    // termination of the executor.
                    try {
                        // physical device means all virtual devices grouped by physical device
                        Map<Integer, List<Device>> physicalDevice = deviceList.getDevicesByNodeId(mConfig.getNodeId());
                        if (physicalDevice != null) {
                            logger.debug("Z-Wave device with node id {} found with {} virtual devices",
                                    mConfig.getNodeId(), physicalDevice.get(mConfig.getNodeId()).size());

                            for (Map.Entry<Integer, List<Device>> entry : physicalDevice.entrySet()) {
                                logger.debug("Add channels for physical device with node id: {}", mConfig.getNodeId());

                                List<Device> devices = entry.getValue();

                                for (Device device : devices) {
                                    if (device.getVisibility() && !device.getPermanentlyHidden()) {
                                        logger.debug("Add channel for virtual device: {}",
                                                device.getMetrics().getTitle());
                                        addDeviceAsChannel(device);
                                    } else {
                                        logger.debug("Device {} has been skipped, because it was hidden in Z-Way.",
                                                device.getMetrics().getTitle());
                                    }
                                }
                            }
                        } else {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                                    "Z-Way physical device with node id " + mConfig.getNodeId() + " not found.");
                        }

                        // Check command classes (only for ThermostatMode)
                        ZWaveDevice zwaveDevice = getZWayBridgeHandler().getZWayApi()
                                .getZWaveDevice(mConfig.getNodeId());
                        if (!zwaveDevice.getInstances().get0().getCommandClasses().get64().getName().equals("")) {
                            // Load available thermostat modes
                            Map<Integer, String> modes = zwaveDevice.getInstances().get0().getCommandClasses().get64()
                                    .getThermostatModes();

                            logger.debug(
                                    "Z-Wave device implements command class ThermostatMode with the following modes: {}",
                                    modes.toString());

                            addCommandClassThermostatModeAsChannel(modes, mConfig.getNodeId());
                        }

                        // starts polling job and register all linked items
                        completeInitialization();
                    } catch (Throwable t) {
                        if (t instanceof Exception) {
                            logger.error(((Exception) t).getMessage());
                        } else if (t instanceof Error) {
                            logger.error(((Error) t).getMessage());
                        } else {
                            logger.error("Unexpected error");
                        }
                        if (getThing().getStatus() == ThingStatus.ONLINE) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                                    "Error occurred when adding device as channel.");
                        }
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                            "Devices not loaded");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "Z-Way bridge handler not found or not ONLINE.");
            }
        }
    };

    public ZWayZWaveDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Z-Way device handler ...");

        // Set thing status to a valid status
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                "Checking configuration and bridge...");

        // Configuration - thing status update with a error message
        mConfig = loadAndCheckConfiguration();

        if (mConfig != null) {
            logger.debug("Configuration complete: {}", mConfig);

            // Start an extra thread to check the connection, because it takes sometimes more
            // than 5000 milliseconds and the handler will suspend (ThingStatus.UNINITIALIZED).
            scheduler.schedule(new Initializer(), 2, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Z-Way node id required!");
        }
    }

    private void completeInitialization() {
        super.initialize(); // starts polling job and register all linked items
    }

    private ZWayZWaveDeviceConfiguration loadAndCheckConfiguration() {
        ZWayZWaveDeviceConfiguration config = getConfigAs(ZWayZWaveDeviceConfiguration.class);

        if (config.getNodeId() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Z-Wave device couldn't create, because the node id is missing.");
            return null;
        }

        return config;
    }

    @Override
    public void dispose() {
        logger.debug("Dispose Z-Way Z-Wave handler ...");

        if (mConfig.getNodeId() != null) {
            mConfig.setNodeId(null);
        }

        super.dispose();
    }
}
