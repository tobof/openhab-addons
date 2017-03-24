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

import org.openhab.binding.mysensors.internal.event.MySensorsEventRegister;
import org.openhab.binding.mysensors.internal.gateway.MySensorsGatewayConfig;
import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;
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
                topicPublish = myGatewayConfig.getTopicPublish();
                brokerName = myGatewayConfig.getBrokerName();

                mqttConsumer.setTopic(topicSubscribe + "/+/+/+/+/+");

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

            logger.debug("MQTT message received. Topic: {}, Message: {}", topic, payload.toString());
            String[] splitMessage = topic.split("/");
            if (topic.contains(topicSubscribe) && splitMessage.length == 6) {

                String nodeId = splitMessage[1];
                String childId = splitMessage[2];
                String msgType = splitMessage[3];
                String ack = splitMessage[4];
                String subType = splitMessage[5];
                String messagetext = nodeId + ";" + childId + ";" + msgType + ";" + ack + ";" + subType + ";"
                        + payload.toString() + "\n";
                logger.debug("Converted MQTT message to MySensors Serial format. Sending on to bridge: {}",
                        messagetext);
                try {
                    out.write(messagetext.getBytes());
                } catch (IOException e) {
                    logger.debug("Unable to send message to bridge: {}", e.toString());
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
            logger.debug("Splitting Message");
            String[] splitMessage = mySensorsMessage.split(";");
            if (splitMessage.length > 5) {

                String nodeId = splitMessage[0];
                String childId = splitMessage[1];
                String msgType = splitMessage[2];
                String ack = splitMessage[3];
                String subType = splitMessage[4];
                String payload = splitMessage[5];

                String newTopic = topicPublish + "/" + nodeId + "/" + childId + "/" + msgType + "/" + ack + "/"
                        + subType;
                logger.debug("Publishing message: Topic: {}, Message: {}", newTopic, payload);
                this.channel.publish(newTopic, payload.getBytes());

            } else {
                logger.debug("Invalid message: {}", mySensorsMessage);
                return;
            }
        }

    }

}
