/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.discovery;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.PARAMETER_BROKERNAME;
import static org.openhab.binding.mysensors.MySensorsBindingConstants.PARAMETER_CHILDID;
import static org.openhab.binding.mysensors.MySensorsBindingConstants.PARAMETER_NODEID;
import static org.openhab.binding.mysensors.MySensorsBindingConstants.PARAMETER_TOPICPUBLISH;
import static org.openhab.binding.mysensors.MySensorsBindingConstants.PARAMETER_TOPICSUBSCRIBE;
import static org.openhab.binding.mysensors.MySensorsBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.mysensors.MySensorsBindingConstants.THING_TYPE_BRIDGE_MQTT;
import static org.openhab.binding.mysensors.MySensorsBindingConstants.THING_UID_MAP;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokersObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.eclipse.smarthome.io.transport.mqtt.MqttMessageSubscriber;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.openhab.binding.mysensors.handler.MySensorsBridgeHandler;
import org.openhab.binding.mysensors.internal.event.MySensorsGatewayEventListener;
import org.openhab.binding.mysensors.internal.protocol.mqtt.MySensorsMqttService;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for MySensors devices. Starts DiscoveryThread to listen for
 * new things / nodes.
 *
 * @author Tim Oberföll
 *
 */
public class MySensorsDiscoveryService extends AbstractDiscoveryService 
    implements MySensorsGatewayEventListener, MqttBrokersObserver, MqttMessageSubscriber {

    private Logger logger = LoggerFactory.getLogger(MySensorsDiscoveryService.class);

    private MySensorsBridgeHandler bridgeHandler = null;
    
    private final String mqttTopic = "+/+/+/+/+/+";
    
    private final String MQTT_GATEWAY_REPRESENTATION = "0/0/3/0/14";
    
    private String brokerName;
    
    private static MySensorsDiscoveryService instance;
    
    public static MySensorsDiscoveryService getInstance() {
        return instance;
    }
    
    public MySensorsDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 500, true);
        instance = this;
    }

    @Override
    protected void startScan() {
        logger.debug("Starting MySensors discovery scan");
        
        if(bridgeHandler != null)
            bridgeHandler.getMySensorsGateway().addEventListener(this);
        
        MqttService mqttService = MySensorsMqttService.getMqttService();
        
        if(mqttService != null) {
            mqttService.addBrokersListener(this);
            for (MqttBrokerConnection c : mqttService.getAllBrokerConnections()) {
                brokerAdded(c);
            }
        }
    }

    public void activate() {
        startScan();
    }

    @Override
    public void deactivate() {
        stopScan();
    }

    @Override
    protected void stopScan() {
        logger.debug("Stopping MySensors discovery scan");
        if(bridgeHandler != null)
            bridgeHandler.getMySensorsGateway().removeEventListener(this);
        
        MqttService mqttService = MySensorsMqttService.getMqttService();
        if(mqttService != null && brokerName != null) {
            MqttBrokerConnection conn = mqttService.getBrokerConnection(brokerName);
            if(conn != null) {
                // Does not work as intended, consumer is not really removed
                conn.removeConsumer(this);
                logger.debug("Consumer from broker '{}' removed!", brokerName);
                if(conn.hasConsumers())
                    logger.debug("Still has consumers!");
            }
        }
    }

    /**
     * Gets called if message from the MySensors network was received.
     * Distinguishes if a new thing was discovered.
     *
     * @param msg
     *            MySensors message received from the bridge / gateway.
     */
    public void newNodePresented(MySensorsNode node, MySensorsChild child) {
        /*
         * If a message was received from a not known node, which is not a
         * presentation message, we don't do anything!
         */
        if (child != null) {
            // uid must not contains dots
            ThingTypeUID thingUid = THING_UID_MAP.get(child.getPresentationCode());

            if (thingUid != null) {
                logger.debug("Preparing new thing for inbox: {}", thingUid);

                ThingUID uid = new ThingUID(thingUid, bridgeHandler.getThing().getUID(),
                        thingUid.getId().toLowerCase() + "_" + node.getNodeId() + "_" + child.getChildId());

                Map<String, Object> properties = new HashMap<>(2);
                properties.put(PARAMETER_NODEID, node.getNodeId());
                properties.put(PARAMETER_CHILDID, child.getChildId());
                DiscoveryResult result = DiscoveryResultBuilder.create(uid)
                        .withProperties(properties)
                        .withLabel("MySensors Device (" + node.getNodeId() + ";" + child.getChildId() + ")")
                        .withBridge(bridgeHandler.getThing().getUID())
                        .withTTL(180)
                        .build();
                thingDiscovered(result);

                logger.debug("Discovered device submitted");
            } else {
                logger.warn("Cannot automatic discover thing node: {}, child: {} please insert it manually",
                        node.getNodeId(), child.getChildId());
            }
        }
    }

    @Override
    public void newNodeDiscovered(MySensorsNode node, MySensorsChild child) throws Exception {
        newNodePresented(node, child);
    }

    /**
     * If a broker was added or found in the MqttService class we add this object as consumer
     * to look for MQTT gateway representation.
     */
    @Override
    public void brokerAdded(MqttBrokerConnection broker) {
        logger.debug("Found broker connection {}", broker.getName());
        try {
            MySensorsMqttService.getMqttService().getBrokerConnection(broker.getName()).addConsumer(this);
            brokerName = broker.getName();
        } catch (MqttException e) {
            logger.error("Error while trying to add consumer to broker connection {}", broker.getName(), e);
        }
    }

    @Override
    public void brokerRemoved(MqttBrokerConnection broker) {
        MySensorsMqttService.getMqttService().getBrokerConnection(broker.getName()).removeConsumer(this);
    }

    public MySensorsBridgeHandler getBridgeHandler() {
        return bridgeHandler;
    }

    public void setBridgeHandler(MySensorsBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    
    /**
     * If a broker is present in MqttService and a scan is started, 
     * we'll listen on all topics ("#") for presentation messages
     */
    @Override
    public void processMessage(String topic, byte[] payload) {
        if(topic.contains(MQTT_GATEWAY_REPRESENTATION)) {
            String topicSubscribe = topic.split("/")[0];
            logger.debug("Gateway representation via MQTT received with topic: {}", topicSubscribe);
            
            String topicPublish = guessTopicPublish(topicSubscribe);
            
            ThingUID thingUID = new ThingUID(THING_TYPE_BRIDGE_MQTT, topicSubscribe);

            Map<String, Object> properties = new HashMap<>(2);
            properties.put(PARAMETER_TOPICPUBLISH, topicPublish);
            properties.put(PARAMETER_TOPICSUBSCRIBE, topicSubscribe);
            properties.put(PARAMETER_BROKERNAME, brokerName);
            thingDiscovered(DiscoveryResultBuilder.create(thingUID)
                    .withProperties(properties)
                    .withLabel("MySensors MQTT Gateway (" + brokerName + ";" + topicSubscribe + ")")
                    .withTTL(180)
                    .build());

            logger.debug("Discovered MQTT gateway submitted");
        }
    }
    
    /**
     * Method tries to guess the MQTT publish topic based to the subscription topic
     * 
     * @param topicSubscribe The topic the bridge is receiving messages from
     * @return the topic the bridge may send messages to
     */
    private String guessTopicPublish(String topicSubscribe) {
        String topicPublish = "mygateway1-in";
        
        if(topicSubscribe.contains("out")) {
            topicPublish = topicSubscribe.replace("out", "in");
        }
        return topicPublish;
    }

    @Override
    public String getTopic() {
        return mqttTopic;
    }
}
