/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dscalarm.internal.factory;

import static org.openhab.binding.dscalarm.DSCAlarmBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.openhab.binding.dscalarm.DSCAlarmBindingConstants;
import org.openhab.binding.dscalarm.config.*;
import org.openhab.binding.dscalarm.handler.DSCAlarmBaseBridgeHandler;
import org.openhab.binding.dscalarm.handler.EnvisalinkBridgeHandler;
import org.openhab.binding.dscalarm.handler.IT100BridgeHandler;
import org.openhab.binding.dscalarm.handler.PanelThingHandler;
import org.openhab.binding.dscalarm.handler.PartitionThingHandler;
import org.openhab.binding.dscalarm.handler.ZoneThingHandler;
import org.openhab.binding.dscalarm.handler.KeypadThingHandler;
import org.openhab.binding.dscalarm.internal.discovery.DSCAlarmDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

/**
 * The {@link DSCAlarmHandlerFactory} is responsible for creating things and thing. handlers.
 * 
 * @author Russell Stephens - Initial Contribution
 */
public class DSCAlarmHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(DSCAlarmHandlerFactory.class);
    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegistrations = new HashMap<ThingUID, ServiceRegistration<?>>();

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID, ThingUID bridgeUID) {

        if (DSCAlarmBindingConstants.ENVISALINKBRIDGE_THING_TYPE.equals(thingTypeUID)) {
            ThingUID envisalinkBridgeUID = getEnvisalinkBridgeThingUID(thingTypeUID, thingUID, configuration);
            logger.debug("createThing(): ENVISALINK_BRIDGE: Creating an '{}' type Thing - {}", thingTypeUID, envisalinkBridgeUID.getId());
            return super.createThing(thingTypeUID, configuration, envisalinkBridgeUID, null);
        } else if (DSCAlarmBindingConstants.IT100BRIDGE_THING_TYPE.equals(thingTypeUID)) {
            ThingUID it100BridgeUID = getIT100BridgeThingUID(thingTypeUID, thingUID, configuration);
            logger.debug("createThing(): IT100_BRIDGE: Creating an '{}' type Thing - {}", thingTypeUID, it100BridgeUID.getId());
            return super.createThing(thingTypeUID, configuration, it100BridgeUID, null);
        } else if (DSCAlarmBindingConstants.PANEL_THING_TYPE.equals(thingTypeUID)) {
            ThingUID panelThingUID = getDSCAlarmPanelUID(thingTypeUID, thingUID, configuration, bridgeUID);
            logger.debug("createThing(): PANEL_THING: Creating '{}' type Thing - {}", thingTypeUID, panelThingUID.getId());
            return super.createThing(thingTypeUID, configuration, panelThingUID, bridgeUID);
        } else if (DSCAlarmBindingConstants.PARTITION_THING_TYPE.equals(thingTypeUID)) {
            ThingUID partitionThingUID = getDSCAlarmPartitionUID(thingTypeUID, thingUID, configuration, bridgeUID);
            logger.debug("createThing(): PARTITION_THING: Creating '{}' type Thing - {}", thingTypeUID, partitionThingUID.getId());
            return super.createThing(thingTypeUID, configuration, partitionThingUID, bridgeUID);
        } else if (DSCAlarmBindingConstants.ZONE_THING_TYPE.equals(thingTypeUID)) {
            ThingUID zoneThingUID = getDSCAlarmZoneUID(thingTypeUID, thingUID, configuration, bridgeUID);
            logger.debug("createThing(): ZONE_THING: Creating '{}' type Thing - {}", thingTypeUID, zoneThingUID.getId());
            return super.createThing(thingTypeUID, configuration, zoneThingUID, bridgeUID);
        } else if (DSCAlarmBindingConstants.KEYPAD_THING_TYPE.equals(thingTypeUID)) {
            ThingUID keypadThingUID = getDSCAlarmKeypadUID(thingTypeUID, thingUID, configuration, bridgeUID);
            logger.debug("createThing(): KEYPAD_THING: Creating '{}' type Thing - {}", thingTypeUID, keypadThingUID.getId());
            return super.createThing(thingTypeUID, configuration, keypadThingUID, bridgeUID);
        }

        throw new IllegalArgumentException("createThing(): The thing type " + thingTypeUID + " is not supported by the DSC Alarm binding.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Get the Envisalink Bridge Thing UID.
     * 
     * @param thingTypeUID
     * @param thingUID
     * @param configuration
     * @return thingUID
     */
    private ThingUID getEnvisalinkBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        if (thingUID == null) {
            String ipAddress = (String) configuration.get(EnvisalinkBridgeConfiguration.IP_ADDRESS);
            String bridgeID = ipAddress.replace('.', '-');
            thingUID = new ThingUID(thingTypeUID, bridgeID);
        }
        return thingUID;
    }

    /**
     * Get the IT-100 Bridge Thing UID.
     * 
     * @param thingTypeUID
     * @param thingUID
     * @param configuration
     * @return thingUID
     */
    private ThingUID getIT100BridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        if (thingUID == null) {
            String serialPort = (String) configuration.get(IT100BridgeConfiguration.SERIAL_PORT);
            String bridgeID = serialPort.replace('.', '-');
            thingUID = new ThingUID(thingTypeUID, bridgeID);
        }
        return thingUID;
    }

    /**
     * Get the Panel Thing UID.
     * 
     * @param thingTypeUID
     * @param thingUID
     * @param configuration
     * @param bridgeUID
     * @return thingUID
     */
    private ThingUID getDSCAlarmPanelUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration, ThingUID bridgeUID) {
        if (thingUID == null) {
            String panelId = "panel";
            thingUID = new ThingUID(thingTypeUID, panelId, bridgeUID.getId());
        }
        return thingUID;
    }

    /**
     * Get the Partition Thing UID.
     * 
     * @param thingTypeUID
     * @param thingUID
     * @param configuration
     * @param bridgeUID
     * @return thingUID
     */
    private ThingUID getDSCAlarmPartitionUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration, ThingUID bridgeUID) {
        if (thingUID == null) {
            String partitionId = "partition" + (String) configuration.get(DSCAlarmPartitionConfiguration.PARTITION_NUMBER);
            thingUID = new ThingUID(thingTypeUID, partitionId, bridgeUID.getId());
        }
        return thingUID;
    }

    /**
     * Get the Zone Thing UID.
     * 
     * @param thingTypeUID
     * @param thingUID
     * @param configuration
     * @param bridgeUID
     * @return thingUID
     */
    private ThingUID getDSCAlarmZoneUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration, ThingUID bridgeUID) {
        if (thingUID == null) {
            String zoneId = "zone" + (String) configuration.get(DSCAlarmZoneConfiguration.ZONE_NUMBER);
            thingUID = new ThingUID(thingTypeUID, zoneId, bridgeUID.getId());
        }
        return thingUID;
    }

    /**
     * Get the Keypad Thing UID.
     * 
     * @param thingTypeUID
     * @param thingUID
     * @param configuration
     * @param bridgeUID
     * @return thingUID
     */
    private ThingUID getDSCAlarmKeypadUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration, ThingUID bridgeUID) {
        if (thingUID == null) {
            String keypadId = "keypad";
            thingUID = new ThingUID(thingTypeUID, keypadId, bridgeUID.getId());
        }
        return thingUID;
    }

    /**
     * Register the Thing Discovery Service for a bridge.
     * 
     * @param dscAlarmBridgeHandler
     */
    private void registerDSCAlarmDiscoveryService(DSCAlarmBaseBridgeHandler dscAlarmBridgeHandler) {
        DSCAlarmDiscoveryService discoveryService = new DSCAlarmDiscoveryService(dscAlarmBridgeHandler);
        discoveryService.activate();

        ServiceRegistration<?> discoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>());
        discoveryServiceRegistrations.put(dscAlarmBridgeHandler.getThing().getUID(), discoveryServiceRegistration);

        logger.debug("registerDSCAlarmDiscoveryService(): Bridge Handler - {}, Class Name - {}, Discovery Service - {}", dscAlarmBridgeHandler, DiscoveryService.class.getName(), discoveryService);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(DSCAlarmBindingConstants.ENVISALINKBRIDGE_THING_TYPE)) {
            EnvisalinkBridgeHandler handler = new EnvisalinkBridgeHandler((Bridge) thing);
            registerDSCAlarmDiscoveryService(handler);
            logger.debug("createHandler(): ENVISALINKBRIDGE_THING: ThingHandler created for {}", thingTypeUID);
            return (EnvisalinkBridgeHandler) handler;
        } else if (thingTypeUID.equals(DSCAlarmBindingConstants.IT100BRIDGE_THING_TYPE)) {
            IT100BridgeHandler handler = new IT100BridgeHandler((Bridge) thing);
            registerDSCAlarmDiscoveryService(handler);
            logger.debug("createHandler(): IT100BRIDGE_THING: ThingHandler created for {}", thingTypeUID);
            return (IT100BridgeHandler) handler;
        } else if (thingTypeUID.equals(DSCAlarmBindingConstants.PANEL_THING_TYPE)) {
            logger.debug("createHandler(): PANEL_THING: ThingHandler created for {}", thingTypeUID);
            return new PanelThingHandler(thing);
        } else if (thingTypeUID.equals(DSCAlarmBindingConstants.PARTITION_THING_TYPE)) {
            logger.debug("createHandler(): PARTITION_THING: ThingHandler created for {}", thingTypeUID);
            return new PartitionThingHandler(thing);
        } else if (thingTypeUID.equals(DSCAlarmBindingConstants.ZONE_THING_TYPE)) {
            logger.debug("createHandler(): ZONE_THING: ThingHandler created for {}", thingTypeUID);
            return new ZoneThingHandler(thing);
        } else if (thingTypeUID.equals(DSCAlarmBindingConstants.KEYPAD_THING_TYPE)) {
            logger.debug("createHandler(): KEYPAD_THING: ThingHandler created for {}", thingTypeUID);
            return new KeypadThingHandler(thing);
        } else {
            logger.debug("createHandler(): ThingHandler not found for {}", thingTypeUID);
            return null;
        }
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        ServiceRegistration<?> discoveryServiceRegistration = discoveryServiceRegistrations.get(thingHandler.getThing().getUID());

        if (discoveryServiceRegistration != null) {
            DSCAlarmDiscoveryService discoveryService = (DSCAlarmDiscoveryService) bundleContext.getService(discoveryServiceRegistration.getReference());
            discoveryService.deactivate();
            discoveryServiceRegistration.unregister();
            discoveryServiceRegistration = null;
            discoveryServiceRegistrations.remove(thingHandler.getThing().getUID());
        }

        super.removeHandler(thingHandler);
    }

}
