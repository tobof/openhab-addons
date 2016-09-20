/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.handler;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.mysensors.config.MySensorsBridgeConfiguration;
import org.openhab.binding.mysensors.internal.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.protocol.ip.MySensorsIpConnection;
import org.openhab.binding.mysensors.protocol.serial.MySensorsSerialConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tim Oberföll
 *
 *         MySensorsBridgeHandler is used to initialize a new bridge (in MySensors: Gateway)
 *         The sensors are connected via the gateway/bridge to the controller
 */
public class MySensorsBridgeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(MySensorsBridgeHandler.class);

    // Bridge connection
    private MySensorsBridgeConnection myCon = null;

    // Configuration from thing file
    private MySensorsBridgeConfiguration myConfiguration = null;

    public MySensorsBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#initialize()
     */
    @Override
    public void initialize() {
        logger.debug("Initialization of the MySensors bridge");

        notifyDisconnect();

        myConfiguration = getConfigAs(MySensorsBridgeConfiguration.class);

        if (getThing().getThingTypeUID().equals(THING_TYPE_BRIDGE_SER)) {
            myCon = new MySensorsSerialConnection(this, myConfiguration.serialPort, myConfiguration.baudRate,
                    myConfiguration.sendDelay);
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_BRIDGE_ETH)) {
            myCon = new MySensorsIpConnection(this, myConfiguration.ipAddress, myConfiguration.tcpPort,
                    myConfiguration.sendDelay);
        } else {
            logger.error("Not recognized bridge: {}", getThing().getThingTypeUID());
        }

        if (myCon != null) {
            myCon.initialize();
        }
    }

    @Override
    public void preDispose() {
        super.preDispose();
        notifyDisconnect();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#dispose()
     */
    @Override
    public void dispose() {
        logger.debug("Disposing of the MySensors bridge");
        myCon.destroy();
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

    public ThingRegistry getThingRegistry() {
        return thingRegistry;
    }

    public MySensorsBridgeConfiguration getBridgeConfiguration() {
        return myConfiguration;
    }

    public MySensorsBridgeConnection getBridgeConnection() {
        return myCon;
    }

    public void notifyConnect() {
        updateStatus(ThingStatus.ONLINE);
    }

    public void notifyDisconnect() {
        updateStatus(ThingStatus.OFFLINE);
    }
}
