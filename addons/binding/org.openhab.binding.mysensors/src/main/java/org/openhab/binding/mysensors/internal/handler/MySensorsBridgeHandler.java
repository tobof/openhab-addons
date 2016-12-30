/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.handler;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.List;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.mysensors.config.MySensorsBridgeConfiguration;
import org.openhab.binding.mysensors.internal.event.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.internal.event.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.factory.MySensorsCacheFactory;
import org.openhab.binding.mysensors.internal.protocol.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.protocol.ip.MySensorsIpConnection;
import org.openhab.binding.mysensors.internal.protocol.serial.MySensorsSerialConnection;
import org.openhab.binding.mysensors.internal.sensors.MySensorsDeviceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tim Oberföll
 *
 *         MySensorsBridgeHandler is used to initialize a new bridge (in MySensors: Gateway)
 *         The sensors are connected via the gateway/bridge to the controller
 */
public class MySensorsBridgeHandler extends BaseBridgeHandler implements MySensorsUpdateListener {

    private Logger logger = LoggerFactory.getLogger(MySensorsBridgeHandler.class);

    // Bridge connection
    private MySensorsBridgeConnection myCon = null;

    // Device manager
    private MySensorsDeviceManager deviceManager;

    // Configuration from thing file
    private MySensorsBridgeConfiguration myConfiguration = null;

    public MySensorsBridgeHandler(MySensorsDeviceManager deviceManager, Bridge bridge) {
        super(bridge);
        this.deviceManager = deviceManager;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#initialize()
     */
    @Override
    public void initialize() {
        logger.debug("Initialization of the MySensors bridge");

        myConfiguration = getConfigAs(MySensorsBridgeConfiguration.class);

        if (getThing().getThingTypeUID().equals(THING_TYPE_BRIDGE_SER)) {
            myCon = new MySensorsSerialConnection(deviceManager, this, myConfiguration.serialPort,
                    myConfiguration.baudRate, myConfiguration.sendDelay);
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_BRIDGE_ETH)) {
            myCon = new MySensorsIpConnection(deviceManager, this, myConfiguration.ipAddress, myConfiguration.tcpPort,
                    myConfiguration.sendDelay);
        } else {
            logger.error("Not recognized bridge: {}", getThing().getThingTypeUID());
        }

        if (myCon != null) {
            myCon.initialize();
            myCon.addEventListener(this);
        }

        logger.debug("Initialization of the MySensors bridge DONE!");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#dispose()
     */
    @Override
    public void dispose() {
        logger.debug("Disposing of the MySensors bridge");
        if (myCon != null) {
            myCon.removeEventListener(this);
            myCon.destroy();
        }

        updateCacheFile();

        super.dispose();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.ThingHandler#handleCommand(org.eclipse.smarthome.core.thing.ChannelUID,
     * org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    public MySensorsBridgeConfiguration getBridgeConfiguration() {
        return myConfiguration;
    }

    public MySensorsBridgeConnection getBridgeConnection() {
        return myCon;
    }

    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {
        switch (event.getEventType()) {
            case NEW_NODE_DISCOVERED:
                updateCacheFile();
                break;
            case BRIDGE_STATUS_UPDATE:
                if (((MySensorsBridgeConnection) event.getData()).isConnected()) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
                break;
            default:
                break;
        }

    }

    private void updateCacheFile() {
        MySensorsCacheFactory cacheFactory = MySensorsCacheFactory.getCacheFactory();

        List<Integer> givenIds = deviceManager.getGivenIds();

        cacheFactory.writeCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE, givenIds.toArray(new Integer[] {}),
                Integer[].class);
    }

    @Override
    public String toString() {
        return "MySensorsBridgeHandler []";
    }

}
