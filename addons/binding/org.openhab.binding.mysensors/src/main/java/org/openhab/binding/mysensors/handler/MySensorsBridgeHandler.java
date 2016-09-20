/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.handler;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.mysensors.config.MySensorsBridgeConfiguration;
import org.openhab.binding.mysensors.internal.MySensorsNetworkConnector;
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

    // Network connector to bridge
    private MySensorsNetworkConnector mysConnector = null;
    private Future<?> connectorFuture = null;

    // Configuration from thing file
    private MySensorsBridgeConfiguration myConfiguration = null;

    private ScheduledExecutorService connectorScheduler = null;

    public MySensorsBridgeHandler(Bridge bridge) {
        super(bridge);
        connectorScheduler = Executors.newSingleThreadScheduledExecutor();
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

        logger.debug("Set skip check on startup to: {}", myConfiguration.skipStartupCheck);

        mysConnector = new MySensorsNetworkConnector(this);
        connectorFuture = connectorScheduler.scheduleWithFixedDelay(mysConnector, 0,
                MySensorsNetworkConnector.CONNECTOR_INTERVAL_CHECK, TimeUnit.SECONDS);

        notifyDisconnect();
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
        disconnect();
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
        // TODO Auto-generated method stub
    }

    public ThingRegistry getThingRegistry() {
        return thingRegistry;
    }

    public MySensorsNetworkConnector getBridgeConnector() {
        return mysConnector;
    }

    public MySensorsBridgeConfiguration getBridgeConfiguration() {
        return myConfiguration;
    }

    public void notifyConnect() {
        updateStatus(ThingStatus.ONLINE);
    }

    public void notifyDisconnect() {
        updateStatus(ThingStatus.OFFLINE);
    }

    private void disconnect() {

        if (mysConnector != null) {
            mysConnector.stop();
            mysConnector = null;
        }

        if (connectorFuture != null) {
            connectorFuture.cancel(true);
            connectorFuture = null;
        }

        if (connectorScheduler != null) {
            connectorScheduler.shutdown();
            connectorScheduler.shutdownNow();
            connectorScheduler = null;
        }
    }
}
