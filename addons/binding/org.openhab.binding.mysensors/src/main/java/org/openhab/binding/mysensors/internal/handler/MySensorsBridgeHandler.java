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
    private MySensorsDeviceManager myDevManager = null;

    // Configuration from thing file
    private MySensorsBridgeConfiguration myConfiguration = null;

    // Cache file
    private MySensorsCacheFactory bindingCacheFile = null;

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
            myCon.addEventListener(this);
        }

        myDevManager = new MySensorsDeviceManager(myCon);

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

    public MySensorsDeviceManager getDeviceManager() {
        return myDevManager;
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
        /*
         * MySensorsCacheFactory cacheFactory = MySensorsCacheFactory.getCacheFactory();
         *
         * List<Integer> givenIds = IntStream
         * .of(cacheFactory.readCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE, new int[] {}, int[].class))
         * .boxed().collect(Collectors.toList());
         * List<Number> takenIds = new ArrayList<Number>();
         *
         * // Which ids are already given by the binding, but not yet in the thing list?
         * Iterator<Integer> iteratorGiven = givenIds.iterator();
         * while (iteratorGiven.hasNext()) {
         * takenIds.add(iteratorGiven.next());
         * }
         *
         * // Which ids are taken in Thing list of OpenHAB
         * Collection<Thing> thingList = thingRegistry.getAll();
         * Iterator<Thing> iterator = thingList.iterator();
         *
         * while (iterator.hasNext()) {
         * Thing thing = iterator.next();
         * Configuration conf = thing.getConfiguration();
         * if (conf != null) {
         * Object nodeIdobj = conf.get("nodeId");
         * if (nodeIdobj != null) {
         * int nodeId = Integer.parseInt(nodeIdobj.toString());
         * takenIds.add(nodeId);
         * }
         * }
         * }
         *
         * cacheFactory.writeCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE, givenIds.toArray(new Integer[] {}),
         * Integer[].class);
         */
    }
}
