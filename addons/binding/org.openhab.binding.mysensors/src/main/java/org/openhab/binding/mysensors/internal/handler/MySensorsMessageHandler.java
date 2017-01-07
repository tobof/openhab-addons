package org.openhab.binding.mysensors.internal.handler;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import org.openhab.binding.mysensors.config.MySensorsBridgeConfiguration;
import org.openhab.binding.mysensors.internal.event.MySensorsBridgeConnectionEventListener;
import org.openhab.binding.mysensors.internal.exception.NoMoreIdsException;
import org.openhab.binding.mysensors.internal.protocol.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsDeviceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySensorsMessageHandler implements MySensorsBridgeConnectionEventListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private MySensorsBridgeConnection myCon;

    private MySensorsBridgeConfiguration myConnConf;

    private MySensorsDeviceManager myDevMan;

    public MySensorsMessageHandler(MySensorsBridgeConnection myCon, MySensorsDeviceManager myDevMan,
            MySensorsBridgeConfiguration myConnConf) {
        this.myCon = myCon;
        this.myDevMan = myDevMan;
        this.myConnConf = myConnConf;
    }

    @Override
    public void messageReceived(MySensorsMessage message) throws Throwable {
        handleSpecialMessageEvent(message);
    }

    private void handleSpecialMessageEvent(MySensorsMessage msg) {
        // Do we get an ACK?
        if (msg.getAck() == 1) {
            logger.debug(String.format("ACK received! Node: %d, Child: %d", msg.nodeId, msg.childId));
            myCon.removeMySensorsOutboundMessage(msg);
        }

        // Have we get a I_CONFIG message?
        if (msg.isIConfigMessage()) {
            answerIConfigMessage(msg);
        }

        // Have we get a I_TIME message?
        if (msg.isITimeMessage()) {
            answerITimeMessage(msg);
        }

        // Requesting ID
        if (msg.isIdRequestMessage()) {
            answerIDRequest();
        }
    }

    /**
     * Answer to I_TIME message for gateway time request from sensor
     *
     * @param msg, the incoming I_TIME message from sensor
     */
    private void answerITimeMessage(MySensorsMessage msg) {
        logger.info("I_TIME request received from {}, answering...", msg.nodeId);

        String time = Long.toString(System.currentTimeMillis() / 1000);
        MySensorsMessage newMsg = new MySensorsMessage(msg.nodeId, msg.childId, MYSENSORS_MSG_TYPE_INTERNAL, 0, false,
                MYSENSORS_SUBTYPE_I_TIME, time);
        myCon.addMySensorsOutboundMessage(newMsg);

    }

    /**
     * Answer to I_CONFIG message for imperial/metric request from sensor
     *
     * @param msg, the incoming I_CONFIG message from sensor
     */
    private void answerIConfigMessage(MySensorsMessage msg) {
        boolean imperial = myConnConf.imperial;
        String iConfig = imperial ? "I" : "M";

        logger.debug("I_CONFIG request received from {}, answering: (is imperial?){}", iConfig, imperial);

        MySensorsMessage newMsg = new MySensorsMessage(msg.nodeId, msg.childId, MYSENSORS_MSG_TYPE_INTERNAL, 0, false,
                MYSENSORS_SUBTYPE_I_CONFIG, iConfig);
        myCon.addMySensorsOutboundMessage(newMsg);

    }

    /**
     * If an ID-Request from a sensor is received the controller will send an id to the sensor
     */
    private void answerIDRequest() {
        logger.info("ID Request received");

        int newId = 0;
        try {
            newId = myDevMan.reserveId();
            logger.info("New Node in the MySensors network has requested an ID. ID is: {}", newId);
            MySensorsMessage newMsg = new MySensorsMessage(255, 255, 3, 0, false, 4, newId + "");
            myCon.addMySensorsOutboundMessage(newMsg);
        } catch (NoMoreIdsException e) {
            logger.error("No more IDs available for this node, you could try cleaning cache file");
        }
    }
}
