/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.handler;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.mysensors.config.MySensorsBridgeConfiguration;
import org.openhab.binding.mysensors.internal.event.MySensorsBridgeConnectionEventListener;
import org.openhab.binding.mysensors.internal.protocol.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.protocol.ip.MySensorsIpConnection;
import org.openhab.binding.mysensors.internal.protocol.serial.MySensorsSerialConnection;
import org.openhab.binding.mysensors.internal.sensors.MySensorsDeviceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MySensorsBridgeHandler is used to initialize a new bridge (in MySensors: Gateway)
 * The sensors are connected via the gateway/bridge to the controller
 *
 * @author Tim Oberföll
 *
 */
public class MySensorsBridgeHandler extends BaseBridgeHandler implements MySensorsBridgeConnectionEventListener {

    private Logger logger = LoggerFactory.getLogger(MySensorsBridgeHandler.class);

    // Bridge connection
    private MySensorsBridgeConnection myCon;

    // Device manager
    private MySensorsDeviceManager deviceManager;

    // Update cache when event arrives
    private MySensorsCacheUpdateHandler cacheUpdateHandler;

    private MySensorsMessageHandler messageHandler;

    // Configuration from thing file
    private MySensorsBridgeConfiguration myConfiguration = null;

    public MySensorsBridgeHandler(MySensorsDeviceManager deviceManager, Bridge bridge) {
        super(bridge);
        this.deviceManager = deviceManager;
        this.cacheUpdateHandler = new MySensorsCacheUpdateHandler(deviceManager);

    }

    @Override
    public void initialize() {
        logger.debug("Initialization of the MySensors bridge");

        myConfiguration = getConfigAs(MySensorsBridgeConfiguration.class);

        if (getThing().getThingTypeUID().equals(THING_TYPE_BRIDGE_SER)) {
            myCon = new MySensorsSerialConnection(getBridgeConfiguration());
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_BRIDGE_ETH)) {
            myCon = new MySensorsIpConnection(getBridgeConfiguration());
        } else {
            logger.error("Not recognized bridge: {}", getThing().getThingTypeUID());
        }

        if (myCon != null) {
            myCon.initialize();

            myCon.addEventListener(this);
            myCon.addEventListener(cacheUpdateHandler);
            myCon.addEventListener(deviceManager);
            myCon.addEventListener(messageHandler);
            deviceManager.addEventListener(cacheUpdateHandler);

        }

        logger.debug("Initialization of the MySensors bridge DONE!");
    }

    @Override
    public void dispose() {
        logger.debug("Disposing of the MySensors bridge");

        if (myCon != null) {
            myCon.clearAllListeners();
            deviceManager.clearAllListeners();

            myCon.destroy();
        }

        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    /**
     * Getter for the configuration of the bridge.
     *
     * @return Configuration of the MySensors bridge.
     */
    public MySensorsBridgeConfiguration getBridgeConfiguration() {
        return myConfiguration;
    }

    /**
     * Getter for the connection to the MySensors bridge / gateway.
     * Used for receiving (register handler) and sending of messages.
     *
     * @return Connection to the MySensors bridge / gateway.
     */
    public MySensorsBridgeConnection getBridgeConnection() {
        return myCon;
    }

    @Override
    public void bridgeStatusUpdate(MySensorsBridgeConnection connection, boolean connected) throws Throwable {
        if (connected) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }

    }

    @Override
    public String toString() {
        return "MySensorsBridgeHandler []";
    }

}
