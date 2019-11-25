/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mysensors.factory;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.mysensors.discovery.MySensorsDiscoveryService;
import org.openhab.binding.mysensors.handler.MySensorsBridgeHandler;
import org.openhab.binding.mysensors.handler.MySensorsThingHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MySensorsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tim Oberföll - Initial contribution
 */
@Component(service = { ThingHandlerFactory.class })
public class MySensorsHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(getClass());

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
            MySensorsBridgeHandler bridgeHandler = new MySensorsBridgeHandler((Bridge) thing);
            registerDiscoveryService(bridgeHandler);
            handler = bridgeHandler;
        } else {
            logger.error("Thing {} cannot be configured, is this thing supported by the binding?", thingTypeUID);
        }

        return handler;
    }

    private synchronized void registerDiscoveryService(MySensorsBridgeHandler bridgeHandler) {
        MySensorsDiscoveryService discoveryService = new MySensorsDiscoveryService(bridgeHandler);
        bridgeHandler.setDiscoveryService(discoveryService);
        bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
    }

}
