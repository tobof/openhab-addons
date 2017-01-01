/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.discovery;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.mysensors.internal.handler.MySensorsBridgeHandler;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tim Oberföll
 *
 *         Discoveryservice for MySensors devices
 */
public class MySensorsDiscoveryService extends AbstractDiscoveryService {

    private Logger logger = LoggerFactory.getLogger(MySensorsDiscoveryService.class);

    private MySensorsBridgeHandler bridgeHandler = null;

    private DiscoveryThread discoThread = null;

    public MySensorsDiscoveryService(MySensorsBridgeHandler bridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, 3000, false);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    protected void startScan() {
        if (discoThread == null) {
            discoThread = new DiscoveryThread(this);
        }
        discoThread.start();
    }

    public void activate() {

    }

    @Override
    public void deactivate() {
        if (discoThread == null) {
            discoThread = new DiscoveryThread(this);
        }
        discoThread.stop();
    }

    @Override
    protected void stopScan() {
        if (discoThread == null) {
            discoThread = new DiscoveryThread(this);
        }
        discoThread.stop();
    }

    public void newDevicePresented(MySensorsMessage msg) {

        // Representation Message?
        if (msg.getMsgType() == MYSENSORS_MSG_TYPE_PRESENTATION) {
            logger.debug("Representation Message received");

            // uid must not contains dots
            ThingTypeUID thingUid = THING_UID_MAP.get(msg.getSubType());

            if (thingUid != null) {
                logger.debug("Preparing new thing for inbox: ", thingUid);

                ThingUID uid = new ThingUID(thingUid, bridgeHandler.getThing().getUID(),
                        thingUid.getId().toLowerCase() + "_" + msg.getNodeId() + "_" + msg.getChildId());

                Map<String, Object> properties = new HashMap<>(2);
                properties.put(PARAMETER_NODEID, "" + msg.getNodeId());
                properties.put(PARAMETER_CHILDID, "" + msg.getChildId());
                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                        .withLabel("MySensors Device (" + msg.getNodeId() + ";" + msg.getChildId() + ")")
                        .withBridge(bridgeHandler.getThing().getUID()).build();
                thingDiscovered(result);

                logger.debug("Discovered device submitted");
            } else {
                logger.warn("Cannot automatic discover thing from message: {}", msg);
            }
        }
    }

}
