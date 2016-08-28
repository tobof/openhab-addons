/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
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

    // List of Ids that OpenHAB has given, in response to an id request from a sensor node
    private List<Number> givenIds = new ArrayList<Number>();

    // Network connector to bridge
    private MySensorsNetworkConnector mysConnector = null;
    private Future<?> connectorFuture = null;

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

        myConfiguration = getConfigAs(MySensorsBridgeConfiguration.class);

        logger.debug("Set skip check on startup to: {}", myConfiguration.skipStartupCheck);

        mysConnector = new MySensorsNetworkConnector(this);
        connectorFuture = getScheduler().scheduleWithFixedDelay(mysConnector, 0, 10, TimeUnit.SECONDS);

        notifyDisconnect();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#dispose()
     */
    @Override
    public void dispose() {
        disconnect();
        notifyDisconnect();
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

    public List<Number> getGivedIds() {
        return givenIds;
    }

    public int getFreeId() {
        int id = 1;

        List<Number> takenIds = new ArrayList<Number>();

        // Which ids are taken in Thing list of OpenHAB
        Collection<Thing> thingList = thingRegistry.getAll();
        Iterator<Thing> iterator = thingList.iterator();

        while (iterator.hasNext()) {
            Thing thing = iterator.next();
            Configuration conf = thing.getConfiguration();
            if (conf != null) {
                Object nodeIdobj = conf.get("nodeId");
                if (nodeIdobj != null) {
                    int nodeId = Integer.parseInt(nodeIdobj.toString());
                    takenIds.add(nodeId);
                }
            }
        }

        // Which ids are already given by the binding, but not yet in the thing list?
        Iterator<Number> iteratorGiven = givenIds.iterator();
        while (iteratorGiven.hasNext()) {
            takenIds.add(iteratorGiven.next());
        }

        // generate new id
        boolean foundId = false;
        while (!foundId) {
            Random rand = new Random(System.currentTimeMillis());
            int newId = rand.nextInt((254 - 1) + 1) + 1;
            if (!takenIds.contains(newId)) {
                id = newId;
                foundId = true;
            }
        }

        return id;
    }

    public MySensorsNetworkConnector getBridgeConnector() {
        return mysConnector;
    }

    public MySensorsBridgeConfiguration getBridgeConfiguration() {
        return myConfiguration;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public void notifyConnect() {
        updateStatus(ThingStatus.ONLINE);
    }

    public void notifyDisconnect() {
        updateStatus(ThingStatus.OFFLINE);
    }

    private void disconnect() {

        if (connectorFuture != null) {
            connectorFuture.cancel(true);
            connectorFuture = null;
        }

        if (mysConnector != null) {
            mysConnector.disconnect();
            mysConnector = null;
        }
    }
}
