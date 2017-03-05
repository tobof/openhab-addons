package org.openhab.binding.mysensors.internal.protocol.mqtt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.openhab.binding.mysensors.internal.event.MySensorsEventRegister;
import org.openhab.binding.mysensors.internal.gateway.MySensorsGatewayConfig;
import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;

public class MySensorsMqttConnection extends MySensorsAbstractConnection {

    // private Logger logger = LoggerFactory.getLogger(MySensorsMqttConnection.class);

    // Configuration from thing file
    // private MySensorsBridgeConfiguration myConfiguration = null;

    private MqttAsyncClient mqttClient = null;

    private MqttConnectOptions options = null;

    // private MySensorsBridgeHandler bridgeHandler = null;
    private String url = null;
    private String topic = null;
    private String clientid = null;

    PipedOutputStream out = new PipedOutputStream();

    // private ByteArrayOutputStream out = new ByteArrayOutputStream();
    // private ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

    public MySensorsMqttConnection(MySensorsGatewayConfig myConf, MySensorsEventRegister myEventRegister) {
        super(myConf, myEventRegister);
        // this.url = url;
        // this.topic = topic;
        // myConfiguration = bridgeHandler.getBridgeConfiguration();

        // options = new MqttConnectOptions();
        // Setzen einer Persistent Session
        // options.setCleanSession(false);

    }

    @Override
    public boolean _connect() {
        logger.debug("MQTT Connect Starting");
        if (myGatewayConfig.getClientId() == null || myGatewayConfig.getClientId().isEmpty()) {
            clientid = MqttClient.generateClientId();
        } else {
            clientid = myGatewayConfig.getClientId();
        }
        logger.debug("Client ID: {}", clientid);
        boolean ret = false;
        if (myGatewayConfig.getURL() == null || myGatewayConfig.getURL().isEmpty()) {
            logger.error("URL must not be null or empty");
        } else if (myGatewayConfig.getTopicSubscribe() == null || myGatewayConfig.getTopicSubscribe().isEmpty()) {
            logger.error("Topic must not be null or empty");
        } else {
            url = myGatewayConfig.getURL();
            topic = myGatewayConfig.getTopicSubscribe();
            try {
                logger.debug("Creating MQTT client object: URL {}, Client ID {}", url, clientid);

                mqttClient = new MqttAsyncClient(url, clientid);

                if (myGatewayConfig.getUsername() != null && myGatewayConfig.getPassword() != null) {
                    options = new MqttConnectOptions();
                    options.setUserName(myGatewayConfig.getUsername());
                    options.setPassword(myGatewayConfig.getPassword().toCharArray());
                }

                mqttClient.setCallback(new MyMqttCallback());

                IMqttToken mqttToken = null;

                if (options != null) {
                    mqttToken = mqttClient.connect(options);
                } else {
                    mqttToken = mqttClient.connect();
                }

                mqttToken.waitForCompletion(5000);

                if (mqttToken.isComplete()) {
                    if (mqttToken.getException() != null) {
                        throw mqttToken.getException();
                    } else {
                        logger.debug("Connection to MQTT broker: {} established", url);
                        ret = true;
                    }
                }

                logger.debug("Subscribing to topic: {}", topic);
                mqttClient.subscribe(topic + "/+/+/+/+/+", 0);

                PipedInputStream in = new PipedInputStream(out);
                mysConReader = new MySensorsReader(in);
                mysConWriter = new MySensorsMqttWriter(System.out); // null output rather than stdout?

                ret = startReaderWriterThread(mysConReader, mysConWriter);

            } catch (MqttException | IOException e) {
                logger.error("Error in initialization of the MqttClient: + {}", e.toString());
            }

        }
        return ret;
    }

    @Override
    protected void _disconnect() {
        try {
            logger.debug("Disconnecting MqttClient");
            mqttClient.disconnect();
        } catch (MqttException e) {
            logger.error("Error disconnecting MqttClient: {}", e.toString());
        }

    }

    @Override
    public boolean isConnected() {
        if (mqttClient != null) {
            return mqttClient.isConnected();
        } else {
            return false;
        }
    }

    private class MyMqttCallback implements MqttCallback {
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            logger.debug("MQTT message received. Topic: {}, Message: {}", topic, message);
            if (topic.contains(myGatewayConfig.getTopicSubscribe())) {

                String messagetext = topic.replace(myGatewayConfig.getTopicSubscribe() + "/", "").replace("/", ";")
                        .concat(";" + message + "\n");
                logger.debug("writing message to byte buffer: {}", messagetext);
                out.write(messagetext.getBytes());
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void connectionLost(Throwable arg0) {
            logger.debug("Connection lost: {}", arg0.toString());
        }

    }

    protected class MySensorsMqttWriter extends MySensorsWriter {

        public MySensorsMqttWriter(OutputStream outStream) {
            super(outStream);
        }

        @Override
        protected void sendMessage(String msg) {
            logger.debug("Attempting to send message: {}", msg);
            String topicPublish = myGatewayConfig.getTopicPublish();

            String[] splitMessage = msg.split(";");
            if (splitMessage.length > 5) {

                String nodeId = splitMessage[0];
                String childId = splitMessage[1];
                String msgType = splitMessage[2];
                String ack = splitMessage[3];
                String subType = splitMessage[4];
                String payload = splitMessage[5];

                String newTopic = topicPublish + "/" + nodeId + "/" + childId + "/" + msgType + "/" + ack + "/"
                        + subType;

                MqttMessage newMessage = new MqttMessage(payload.getBytes());

                try {
                    mqttClient.publish(newTopic, newMessage);
                } catch (MqttException e) {
                    logger.error("Error publishing to MQTT broker: {}", e.toString());
                    return;
                }
                logger.debug("Message Sent: Node: {}, Sensor: {}, Type: {}, Ack: {}, SubType:{}, payload: {}", nodeId,
                        childId, msgType, ack, subType, payload);

            } else {
                logger.debug("Invalid message: {}", msg);
                return;
            }

        }

    }

}
