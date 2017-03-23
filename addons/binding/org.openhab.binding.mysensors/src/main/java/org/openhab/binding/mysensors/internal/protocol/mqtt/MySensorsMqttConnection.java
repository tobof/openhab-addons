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
        out = new PipedOutputStream();
        in = new PipedInputStream();
        try {
            in.connect(out);
        } catch (IOException e) {
            logger.debug(e.toString());
        }
        mysConReader = new MySensorsReader(in);
        mysConWriter = new MySensorsMqttWriter(System.out); // null output rather than stdout?
    }

    @Override
    public boolean _connect() {

        boolean ret = false;
        if (MySensorsMqttService.getInstance() != null) {

            mqttService = myMqtt.getService();
            System.out.println("MQTT SERVICE: " + mqttService);

            logger.debug("Getting MQTT gateway configuration");
            topicSubscribe = myGatewayConfig.getTopicSubscribe();
            topicPublish = myGatewayConfig.getTopicPublish();
            brokerName = myGatewayConfig.getBrokerName();
            logger.debug("Setting subscribe topic");
            mqttConsumer.setTopic(topicSubscribe + "/+/+/+/+/+");
            logger.debug("registering consumer" + mqttConsumer);
            mqttService.registerMessageConsumer(brokerName, mqttConsumer);
            logger.debug("registering publisher " + mqttPublisher);
            mqttService.registerMessageProducer(brokerName, mqttPublisher);

            ret = startReaderWriterThread(mysConReader, mysConWriter);
        }

        return ret;
    }

    @Override
    protected void _disconnect() {
        logger.debug("Unregistering MQTT publisher/consumer");
        mqttService.unregisterMessageConsumer(brokerName, mqttConsumer);
        mqttService.unregisterMessageProducer(brokerName, mqttPublisher);
    }

    protected class MySensorsMqttWriter extends MySensorsWriter {

        public MySensorsMqttWriter(OutputStream outStream) {
            super(outStream);
        }

        @Override
        protected void sendMessage(String msg) {

            // topicPublish = myGatewayConfig.getTopicPublish();
            logger.debug("Sending MQTT Message: Topic: {}, Message: {}", topicPublish, msg);
            try {
                mqttPublisher.publish(topicPublish, msg);
            } catch (Exception e) {
                logger.debug("Error sending MQTT message: {}", e.toString());
            }

        }

    }

    public class MySensorsMqttConsumer implements MqttMessageConsumer {

        private EventPublisher ep;
        private String topic;

        @Override
        public void processMessage(String topic, byte[] payload) {

            logger.debug("MQTT message received. Topic: {}, Message: {}", topic, payload);
            String[] splitMessage = topic.split("/");
            if (topic.contains(topicSubscribe) && splitMessage.length == 6) {

                String nodeId = splitMessage[1];
                String childId = splitMessage[2];
                String msgType = splitMessage[3];
                String ack = splitMessage[4];
                String subType = splitMessage[5];
                String messagetext = nodeId + ";" + childId + ";" + msgType + ";" + ack + ";" + subType + ";"
                        + payload.toString() + "\n";
                logger.debug("writing message to byte buffer: {}", messagetext);
                try {
                    out.write(messagetext.getBytes());
                } catch (IOException e) {
                    logger.debug("Unable to write message to byte buffer: {}", e.toString());
                }
            }
        }

        @Override
        public String getTopic() {
            // TODO Auto-generated method stub
            return topic;
        }

        @Override
        public void setTopic(String topic) {
            this.topic = topic;

        }

        @Override
        public void setEventPublisher(EventPublisher eventPublisher) {
            this.ep = eventPublisher;
        }

    }

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
