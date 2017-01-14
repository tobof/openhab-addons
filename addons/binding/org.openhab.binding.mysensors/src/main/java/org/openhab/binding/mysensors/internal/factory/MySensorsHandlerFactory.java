/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.factory;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.mysensors.discovery.MySensorsDiscoveryService;
import org.openhab.binding.mysensors.internal.handler.MySensorsBridgeHandler;
import org.openhab.binding.mysensors.internal.handler.MySensorsThingHandler;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MySensorsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tim Oberföll
 */
public class MySensorsHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(getClass());

    // Discovery services
    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_DEVICE_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        logger.trace("Creating handler for thing: {}", thing.getUID());
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        ThingHandler handler = null;

        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            handler = new MySensorsThingHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_BRIDGE_SER) || thingTypeUID.equals(THING_TYPE_BRIDGE_ETH)) {
            handler = new MySensorsBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService((MySensorsBridgeHandler) handler);
        } else {
            logger.error("Thing {} cannot be configured, is this thing supported by the binding?", thingTypeUID);
        }

        return handler;
    }

    private void registerDeviceDiscoveryService(MySensorsBridgeHandler mySensorsBridgeHandler) {
        MySensorsDiscoveryService discoveryService = new MySensorsDiscoveryService(mySensorsBridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(mySensorsBridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        logger.trace("Removing handler: {}", thingHandler);
        if (thingHandler instanceof MySensorsBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }

        super.removeHandler(thingHandler);
    }
}
