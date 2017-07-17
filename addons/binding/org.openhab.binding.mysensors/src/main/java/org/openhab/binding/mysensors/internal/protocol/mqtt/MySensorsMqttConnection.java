/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol.mqtt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.ParseException;

import org.openhab.binding.mysensors.internal.event.MySensorsEventRegister;
import org.openhab.binding.mysensors.internal.gateway.MySensorsGatewayConfig;
import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.core.events.EventPublisher;
import org.openhab.io.transport.mqtt.MqttMessageConsumer;
import org.openhab.io.transport.mqtt.MqttMessageProducer;
import org.openhab.io.transport.mqtt.MqttSenderChannel;
import org.openhab.io.transport.mqtt.MqttService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the MQTT connection to a gateway of the MySensors network.
 *
 * @author Sean McGuire
 *
 */

public class MySensorsMqttConnection extends MySensorsAbstractConnection {

    private static final Logger logger = LoggerFactory.getLogger(MySensorsMqttConnection.class);

    MySensorsMqttService myMqtt = MySensorsMqttService.getInstance();
    MqttService mqttService = myMqtt.getService();
    MySensorsMqttPublisher mqttPublisher = new MySensorsMqttPublisher();
    MySensorsMqttConsumer mqttConsumer = new MySensorsMqttConsumer();
    String brokerName;
    String topicPublish;
    String topicSubscribe;

    private static PipedOutputStream out;
    private static PipedInputStream in;

    public MySensorsMqttConnection(MySensorsGatewayConfig myConf, MySensorsEventRegister myEventRegister) {
        super(myConf, myEventRegister);

    }

    /**
     * Creates Streams to communicate with the Abstract connection, MQTT producers and consumers
     * to talk to the MQTT broker (via the Openhab MQTT transport bundle) and connects them
     * all together
     */
    @Override
    public boolean establishConnection() {

        boolean ret = false;

        if (myMqtt.getService() != null) {

            in = null;
            out = null;
            out = new PipedOutputStream();
            in = new PipedInputStream();
            try {
                in.connect(out);
                mysConReader = new MySensorsReader(in);
                mysConWriter = new MySensorsMqttWriter(System.out);
                // null output rather than stdout?
                // it should never be written to however, as we have overridden the sendMessage method

                mqttService = myMqtt.getService();

                topicSubscribe = myGatewayConfig.getTopicSubscribe();
                if (topicSubscribe.substring(topicSubscribe.length() - 1) != "/") {
                    topicSubscribe += "/";
                }
                topicPublish = myGatewayConfig.getTopicPublish();
                if (topicPublish.substring(topicPublish.length() - 1) != "/") {
                    topicPublish += "/";
                }
                brokerName = myGatewayConfig.getBrokerName();

                mqttConsumer.setTopic(topicSubscribe + "+/+/+/+/+");

                mqttService.registerMessageConsumer(brokerName, mqttConsumer);
                mqttService.registerMessageProducer(brokerName, mqttPublisher);

                ret = startReaderWriterThread(mysConReader, mysConWriter);

            } catch (IOException e) {
                logger.debug(e.toString());
            }
        }

        return ret;
    }

    /**
     * Cleans up all resources
     */
    @Override
    protected void stopConnection() {
        in = null;
        out = null;
        mqttService.unregisterMessageConsumer(brokerName, mqttConsumer);
        mqttService.unregisterMessageProducer(brokerName, mqttPublisher);
    }

    protected class MySensorsMqttWriter extends MySensorsWriter {

        public MySensorsMqttWriter(OutputStream outStream) {
            super(outStream);
        }

        @Override
        protected void sendMessage(String msg) {

            logger.debug("Sending MQTT Message: Topic: {}, Message: {}", topicPublish, msg);
            try {
                mqttPublisher.publish(topicPublish, msg);
            } catch (NullPointerException ne) {
                logger.debug("Null exception from MQTT transport service, broker unavailable");
                MySensorsMqttConnection.this.requestDisconnection(true);
            } catch (Exception e) {
                logger.debug("Error sending MQTT message: {}", e.toString());
            }

        }

    }

    /**
     * Receives messages from MQTT transport, translates them and passes them on to
     * the MySensors abstract connection
     */
    public class MySensorsMqttConsumer implements MqttMessageConsumer {

        private EventPublisher eventPublisher;
        private String topic;

        @Override
        public void processMessage(String topic, byte[] payload) {
            String payloadString = new String(payload);
            logger.debug("MQTT message received. Topic: {}, Message: {}", topic, payloadString);
            if (topic.indexOf(topicSubscribe) == 0) {

                String messageTopicPart = topic.replace(topicSubscribe, "");
                logger.debug("Message topic part: {}", messageTopicPart);
                MySensorsMessage incomingMessage = new MySensorsMessage();
                try {
                    incomingMessage = MySensorsMessage.parseMQTT(messageTopicPart, payloadString);
                    logger.debug("Converted MQTT message to MySensors Serial format. Sending on to bridge: {}",
                            MySensorsMessage.generateAPIString(incomingMessage).trim());
                    try {

                        out.write(MySensorsMessage.generateAPIString(incomingMessage).getBytes());
                    } catch (IOException ioe) {
                        ioe.toString();
                    }

                } catch (ParseException pe) {
                    logger.debug("Unable to send message to bridge: {}", pe.toString());
                }
            }
        }

        @Override
        public String getTopic() {
            return topic;
        }

        @Override
        public void setTopic(String topic) {
            this.topic = topic;

        }

        @Override
        public void setEventPublisher(EventPublisher eventPublisher) {
            this.eventPublisher = eventPublisher;
        }

        public EventPublisher getEventPublisher(EventPublisher eventPublisher) {
            return this.eventPublisher;
        }

    }

    /**
     * Receives messages from the MySensors abstract connection,
     * translates them and passes them on to the MQTT transport.
     *
     */
    public class MySensorsMqttPublisher implements MqttMessageProducer {

        public MqttSenderChannel channel;

        public MySensorsMqttPublisher() {

        }

        @Override
        public void setSenderChannel(MqttSenderChannel channel) {
            this.channel = channel;
        }

        public void publish(String topicPublish, String mySensorsMessage) throws Exception {

            MySensorsMessage outgoingMessage = new MySensorsMessage();
            outgoingMessage = MySensorsMessage.parse(mySensorsMessage);

            String newTopic = topicPublish + MySensorsMessage.generateMQTTString(outgoingMessage);
            logger.debug("Publishing message: Topic: {}, Message: {}", newTopic, outgoingMessage.getMsg());
            this.channel.publish(newTopic, outgoingMessage.getMsg().getBytes());

        }

    }

}