/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol.message;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to store the content of a MySensors message.
 *
 * @author Tim Oberföll
 *
 */
public class MySensorsMessage {

    // Message types of the MySensors network
    public static final int MYSENSORS_MSG_TYPE_PRESENTATION = 0;
    public static final int MYSENSORS_MSG_TYPE_SET = 1;
    public static final int MYSENSORS_MSG_TYPE_REQ = 2;
    public static final int MYSENSORS_MSG_TYPE_INTERNAL = 3;
    public static final int MYSENSORS_MSG_TYPE_STREAM = 4;

    // NO ACK
    public static final int MYSENSORS_ACK_TRUE = 1;
    public static final int MYSENSORS_ACK_FALSE = 0;

    // Subtypes for presentation
    public static final int MYSENSORS_SUBTYPE_S_DOOR = 0;
    public static final int MYSENSORS_SUBTYPE_S_MOTION = 1;
    public static final int MYSENSORS_SUBTYPE_S_SMOKE = 2;
    public static final int MYSENSORS_SUBTYPE_S_LIGHT = 3;
    public static final int MYSENSORS_SUBTYPE_S_DIMMER = 4;
    public static final int MYSENSORS_SUBTYPE_S_COVER = 5;
    public static final int MYSENSORS_SUBTYPE_S_TEMP = 6;
    public static final int MYSENSORS_SUBTYPE_S_HUM = 7;
    public static final int MYSENSORS_SUBTYPE_S_BARO = 8;
    public static final int MYSENSORS_SUBTYPE_S_WIND = 9;
    public static final int MYSENSORS_SUBTYPE_S_RAIN = 10;
    public static final int MYSENSORS_SUBTYPE_S_UV = 11;
    public static final int MYSENSORS_SUBTYPE_S_WEIGHT = 12;
    public static final int MYSENSORS_SUBTYPE_S_POWER = 13;
    public static final int MYSENSORS_SUBTYPE_S_HEATER = 14;
    public static final int MYSENSORS_SUBTYPE_S_DISTANCE = 15;
    public static final int MYSENSORS_SUBTYPE_S_LIGHT_LEVEL = 16;
    public static final int MYSENSORS_SUBTYPE_S_LOCK = 19;
    public static final int MYSENSORS_SUBTYPE_S_IR = 20;
    public static final int MYSENSORS_SUBTYPE_S_WATER = 21;
    public static final int MYSENSORS_SUBTYPE_S_AIR_QUALITY = 22;
    public static final int MYSENSORS_SUBTYPE_S_CUSTOM = 23;
    public static final int MYSENSORS_SUBTYPE_S_RGB_LIGHT = 26;
    public static final int MYSENSORS_SUBTYPE_S_RGBW_LIGHT = 27;
    public static final int MYSENSORS_SUBTYPE_S_HVAC = 29;
    public static final int MYSENSORS_SUBTYPE_S_MULTIMETER = 30;
    public static final int MYSENSORS_SUBTYPE_S_SPRINKLER = 31;
    public static final int MYSENSORS_SUBTYPE_S_WATER_LEAK = 32;
    public static final int MYSENSORS_SUBTYPE_S_SOUND = 33;
    public static final int MYSENSORS_SUBTYPE_S_VIBRATION = 34;
    public static final int MYSENSORS_SUBTYPE_S_MOISTURE = 35;
    public static final int MYSENSORS_SUBTYPE_S_INFO = 36;
    public static final int MYSENSORS_SUBTYPE_S_GAS = 37;
    public static final int MYSENSORS_SUBTYPE_S_GPS = 38;
    public static final int MYSENSORS_SUBTYPE_S_WATER_QUALITY = 39;

    // Subtypes for set, req
    public static final int MYSENSORS_SUBTYPE_V_TEMP = 0;
    public static final int MYSENSORS_SUBTYPE_V_HUM = 1;
    public static final int MYSENSORS_SUBTYPE_V_STATUS = 2;
    public static final int MYSENSORS_SUBTYPE_V_PERCENTAGE = 3;
    public static final int MYSENSORS_SUBTYPE_V_PRESSURE = 4;
    public static final int MYSENSORS_SUBTYPE_V_FORECAST = 5;
    public static final int MYSENSORS_SUBTYPE_V_RAIN = 6;
    public static final int MYSENSORS_SUBTYPE_V_RAINRATE = 7;
    public static final int MYSENSORS_SUBTYPE_V_WIND = 8;
    public static final int MYSENSORS_SUBTYPE_V_GUST = 9;
    public static final int MYSENSORS_SUBTYPE_V_DIRECTION = 10;
    public static final int MYSENSORS_SUBTYPE_V_UV = 11;
    public static final int MYSENSORS_SUBTYPE_V_WEIGHT = 12;
    public static final int MYSENSORS_SUBTYPE_V_DISTANCE = 13;
    public static final int MYSENSORS_SUBTYPE_V_IMPEDANCE = 14;
    public static final int MYSENSORS_SUBTYPE_V_ARMED = 15;
    public static final int MYSENSORS_SUBTYPE_V_TRIPPED = 16;
    public static final int MYSENSORS_SUBTYPE_V_WATT = 17;
    public static final int MYSENSORS_SUBTYPE_V_KWH = 18;
    public static final int MYSENSORS_SUBTYPE_V_SCENE_ON = 19;
    public static final int MYSENSORS_SUBTYPE_V_SCENE_OFF = 20;
    public static final int MYSENSORS_SUBTYPE_V_HVAC_FLOW_STATE = 21;
    public static final int MYSENSORS_SUBTYPE_V_HVAC_SPEED = 22;
    public static final int MYSENSORS_SUBTYPE_V_LIGHT_LEVEL = 23;
    public static final int MYSENSORS_SUBTYPE_V_VAR1 = 24;
    public static final int MYSENSORS_SUBTYPE_V_VAR2 = 25;
    public static final int MYSENSORS_SUBTYPE_V_VAR3 = 26;
    public static final int MYSENSORS_SUBTYPE_V_VAR4 = 27;
    public static final int MYSENSORS_SUBTYPE_V_VAR5 = 28;
    public static final int MYSENSORS_SUBTYPE_V_UP = 29;
    public static final int MYSENSORS_SUBTYPE_V_DOWN = 30;
    public static final int MYSENSORS_SUBTYPE_V_STOP = 31;
    public static final int MYSENSORS_SUBTYPE_V_IR_SEND = 32;
    public static final int MYSENSORS_SUBTYPE_V_IR_RECEIVE = 33;
    public static final int MYSENSORS_SUBTYPE_V_FLOW = 34;
    public static final int MYSENSORS_SUBTYPE_V_VOLUME = 35;
    public static final int MYSENSORS_SUBTYPE_V_LOCK_STATUS = 36;
    public static final int MYSENSORS_SUBTYPE_V_LEVEL = 37;
    public static final int MYSENSORS_SUBTYPE_V_VOLTAGE = 38;
    public static final int MYSENSORS_SUBTYPE_V_CURRENT = 39;
    public static final int MYSENSORS_SUBTYPE_V_RGB = 40;
    public static final int MYSENSORS_SUBTYPE_V_RGBW = 41;
    public static final int MYSENSORS_SUBTYPE_V_ID = 42;
    public static final int MYSENSORS_SUBTYPE_V_UNIT_PREFIX = 43;
    public static final int MYSENSORS_SUBTYPE_V_HVAC_SETPOINT_COOL = 44;
    public static final int MYSENSORS_SUBTYPE_V_HVAC_SETPOINT_HEAT = 45;
    public static final int MYSENSORS_SUBTYPE_V_HVAC_FLOW_MODE = 46;
    public static final int MYSENSORS_SUBTYPE_V_TEXT = 47;
    public static final int MYSENSORS_SUBTYPE_V_CUSTOM = 48;
    public static final int MYSENSORS_SUBTYPE_V_POSITION = 49;
    public static final int MYSENSORS_SUBTYPE_V_IR_RECORD = 50;
    public static final int MYSENSORS_SUBTYPE_V_PH = 51;
    public static final int MYSENSORS_SUBTYPE_V_ORP = 52;
    public static final int MYSENSORS_SUBTYPE_V_EC = 53;
    public static final int MYSENSORS_SUBTYPE_V_VAR = 54;
    public static final int MYSENSORS_SUBTYPE_V_VA = 55;
    public static final int MYSENSORS_SUBTYPE_V_POWER_FACTOR = 56;

    public static final int MYSENSORS_SUBTYPE_I_BATTERY_LEVEL = 0;
    public static final int MYSENSORS_SUBTYPE_I_TIME = 1;
    public static final int MYSENSORS_SUBTYPE_I_VERSION = 2;
    public static final int MYSENSORS_SUBTYPE_I_ID_REQUEST = 3;
    public static final int MYSENSORS_SUBTYPE_I_ID_RESPONSE = 4;
    public static final int MYSENSORS_SUBTYPE_I_INCLUSION_MODE = 5;
    public static final int MYSENSORS_SUBTYPE_I_CONFIG = 6;
    public static final int MYSENSORS_SUBTYPE_I_FIND_PARENT = 7;
    public static final int MYSENSORS_SUBTYPE_I_FIND_PARENT_RESPONSE = 8;
    public static final int MYSENSORS_SUBTYPE_I_LOG_MESSAGE = 9;
    public static final int MYSENSORS_SUBTYPE_I_CHILDREN = 10;
    public static final int MYSENSORS_SUBTYPE_I_SKETCH_NAME = 11;
    public static final int MYSENSORS_SUBTYPE_I_SKETCH_VERSION = 12;
    public static final int MYSENSORS_SUBTYPE_I_REBOOT = 13;
    public static final int MYSENSORS_SUBTYPE_I_GATEWAY_READY = 14;
    public static final int MYSENSORS_SUBTYPE_I_REQUEST_SIGNING = 15;
    public static final int MYSENSORS_SUBTYPE_I_GET_NONCE = 16;
    public static final int MYSENSORS_SUBTYPE_I_GET_NONCE_RESONSE = 17;
    public static final int MYSENSORS_SUBTYPE_I_HEARTBEAT_REQUEST = 18;
    public static final int MYSENSORS_SUBTYPE_I_PRESENTATION = 19;
    public static final int MYSENSORS_SUBTYPE_I_DISCOVER = 20;
    public static final int MYSENSORS_SUBTYPE_I_DISCOVER_RESPONSE = 21;
    public static final int MYSENSORS_SUBTYPE_I_HEARTBEAT_RESPONSE = 22;
    public static final int MYSENSORS_SUBTYPE_I_LOCKED = 23;
    public static final int MYSENSORS_SUBTYPE_I_PING = 24;
    public static final int MYSENSORS_SUBTYPE_I_PONG = 25;
    public static final int MYSENSORS_SUBTYPE_I_REGISTRATION_REQUEST = 26;
    public static final int MYSENSORS_SUBTYPE_I_REGISTRATION_RESPONSE = 27;
    public static final int MYSENSORS_SUBTYPE_I_DEBUG = 28;

    private Logger logger = LoggerFactory.getLogger(MySensorsMessage.class);

    public int nodeId = 0; // id of the node in the MySensors network
    public int childId = 0; // id of the child of the node (more than one possible)
    public int msgType = 0; // type of message: request, internal, presentation ...
    public int ack = 0; // is an acknoledgement requested?
    public boolean revert = true; // revert status if no ack was received from the node
    public int subType = 0; // like: humidity, temperature, light ...
    public String msg = ""; // content of the message
    public String oldMsg = ""; // content of the last message send to or received by the node
    public int retries = 0; // number of retries if a message is not acknowledged by the receiver
    public long nextSend = 0; // timestamp when the message should be send
    public boolean smartSleep = false; // smartsleep message

    public MySensorsMessage() {

    }

    public MySensorsMessage(int nodeId, int childId, int msgType, int ack, boolean revert) {
        this.nodeId = nodeId;
        this.childId = childId;
        this.msgType = msgType;
        this.ack = ack;
        this.revert = revert;
    }

    public MySensorsMessage(int nodeId, int childId, int msgType, int ack, boolean revert, boolean smartSleep) {
        this.nodeId = nodeId;
        this.childId = childId;
        this.msgType = msgType;
        this.ack = ack;
        this.revert = revert;
        this.smartSleep = smartSleep;
    }

    public MySensorsMessage(int nodeId, int childId, int msgType, int ack, boolean revert, int subType, String msg) {
        this.nodeId = nodeId;
        this.childId = childId;
        this.msgType = msgType;
        this.ack = ack;
        this.revert = revert;
        this.subType = subType;
        this.msg = msg;
    }

    public MySensorsMessage(int nodeId, int childId, int msgType, int ack, boolean revert, int subType, String msg,
            boolean smartSleep) {
        this.nodeId = nodeId;
        this.childId = childId;
        this.msgType = msgType;
        this.ack = ack;
        this.revert = revert;
        this.subType = subType;
        this.msg = msg;
        this.smartSleep = smartSleep;
    }

    /**
     * Write message to DEBUG.
     */
    public void printDebug() {
        logger.debug(getDebugInfo());
    }

    /**
     * @return the content of the message as a String.
     */
    public String getDebugInfo() {
        return String.format("nodeId: %d, childId: %d, msgType: %d, ack: %d, revert: %b, subType: %d ,msg: %s",
                this.nodeId, this.childId, this.msgType, this.ack, this.revert, this.subType, this.msg);
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getChildId() {
        return childId;
    }

    public void setChildId(int childId) {
        this.childId = childId;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public int getAck() {
        return ack;
    }

    public boolean getRevert() {
        return revert;
    }

    public void setAck(int ack) {
        this.ack = ack;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public long getNextSend() {
        return nextSend;
    }

    public void setNextSend(long nextSend) {
        this.nextSend = nextSend;
    }

    public String getOldMsg() {
        return oldMsg;
    }

    public void setOldMsg(String oldMsg) {
        this.oldMsg = oldMsg;
    }

    /**
     * Checks if the received message is a I_CONFIG (internal MySensors) message.
     *
     * @return true, if the received message is a I_CONFIG message.
     */
    public boolean isIConfigMessage() {
        boolean ret = false;

        if (childId == MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_0
                || childId == MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_255) {
            if (msgType == MYSENSORS_MSG_TYPE_INTERNAL) {
                if (ack == MYSENSORS_ACK_FALSE) {
                    if (subType == MYSENSORS_SUBTYPE_I_CONFIG) {
                        ret = true;
                    }
                }
            }
        }

        return ret;
    }

    /**
     * Checks if the received message is a I_VERSION (internal MySensors) message.
     *
     * @return true, if the received message is a I_VERSION message.
     */
    public boolean isIVersionMessage() {
        boolean ret = false;

        if (nodeId == MySensorsNode.MYSENSORS_NODE_ID_RESERVED_0) {
            if (childId == MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_0
                    || childId == MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_255) {
                if (msgType == MYSENSORS_MSG_TYPE_INTERNAL) {
                    if (ack == MYSENSORS_ACK_FALSE) {
                        if (subType == MYSENSORS_SUBTYPE_I_VERSION) {
                            ret = true;
                        }
                    }
                }
            }
        }

        return ret;
    }

    /**
     * Checks if the received message is a I_TIME (internal MySensors) message.
     *
     * @return true, if the received message is a I_TIME message.
     */
    public boolean isITimeMessage() {
        boolean ret = false;

        if (childId == MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_0
                || childId == MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_255) {
            if (msgType == MYSENSORS_MSG_TYPE_INTERNAL) {
                if (ack == MYSENSORS_ACK_FALSE) {
                    if (subType == MYSENSORS_SUBTYPE_I_TIME) {
                        ret = true;
                    }
                }
            }
        }

        return ret;
    }

    /**
     * Checks if the received message is a I_ID_REQUEST (internal MySensors) message.
     *
     * @return true, if the received message is a I_ID_REQUEST message.
     */
    public boolean isIdRequestMessage() {
        boolean ret = false;

        if (nodeId == MySensorsNode.MYSENSORS_NODE_ID_RESERVED_255) {
            if (childId == MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_255) {
                if (msgType == MYSENSORS_SUBTYPE_I_ID_REQUEST) {
                    if (ack == MYSENSORS_ACK_FALSE) {
                        if (subType == MYSENSORS_MSG_TYPE_INTERNAL) {
                            ret = true;
                        }
                    }
                }
            }
        }

        return ret;
    }

    /**
     * Checks if the received message is a presentation message.
     *
     * @return true, if the received message is a presentation message.
     */
    public boolean isPresentationMessage() {
        return msgType == MYSENSORS_MSG_TYPE_PRESENTATION;
    }

    public boolean isSetReqMessage() {
        return msgType == MYSENSORS_MSG_TYPE_REQ || msgType == MYSENSORS_MSG_TYPE_SET;
    }

    public boolean isInternalMessage() {
        return msgType == MYSENSORS_MSG_TYPE_INTERNAL;
    }

    /**
     * Checks if the received message is a heartbeat(response) received from a node
     *
     * @return true, if it is a heartbeat
     */
    public boolean isHeartbeatResponseMessage() {
        return (subType == MYSENSORS_SUBTYPE_I_HEARTBEAT_RESPONSE
                && childId == MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_255);
    }

    /**
     * @param line Input is a String containing the message received from the MySensors network
     * @return Returns the content of the message as a MySensorsMessage
     */
    public static MySensorsMessage parse(String line) {
        String[] splitMessage = line.split(";");
        if (splitMessage.length > 4) {

            MySensorsMessage mysensorsmessage = new MySensorsMessage();

            int nodeId = Integer.parseInt(splitMessage[0]);

            mysensorsmessage.setNodeId(nodeId);
            mysensorsmessage.setChildId(Integer.parseInt(splitMessage[1]));
            mysensorsmessage.setMsgType(Integer.parseInt(splitMessage[2]));
            mysensorsmessage.setAck(Integer.parseInt(splitMessage[3]));
            mysensorsmessage.setSubType(Integer.parseInt(splitMessage[4]));
            if (splitMessage.length == 6) {
                String msg = splitMessage[5].replaceAll("\\r|\\n", "").trim();
                mysensorsmessage.setMsg(msg);
            } else {
                mysensorsmessage.setMsg("");
            }

            return mysensorsmessage;
        } else {
            return null;
        }

    }

    /**
     * Converts a MySensorsMessage object to a String.
     *
     * @param msg the MySensorsMessage that should be converted.
     * @return the MySensorsMessage as a String.
     */
    public static String generateAPIString(MySensorsMessage msg) {
        String APIString = "";
        APIString += msg.getNodeId() + ";";
        APIString += msg.getChildId() + ";";
        APIString += msg.getMsgType() + ";";
        APIString += msg.getAck() + ";";
        APIString += msg.getSubType() + ";";
        APIString += msg.getMsg() + "\n";

        return APIString;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ack;
        result = prime * result + childId;
        result = prime * result + ((msg == null) ? 0 : msg.hashCode());
        result = prime * result + msgType;
        result = prime * result + (int) (nextSend ^ (nextSend >>> 32));
        result = prime * result + nodeId;
        result = prime * result + ((oldMsg == null) ? 0 : oldMsg.hashCode());
        result = prime * result + retries;
        result = prime * result + (revert ? 1231 : 1237);
        result = prime * result + subType;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MySensorsMessage other = (MySensorsMessage) obj;
        if (ack != other.ack) {
            return false;
        }
        if (childId != other.childId) {
            return false;
        }
        if (msg == null) {
            if (other.msg != null) {
                return false;
            }
        } else if (!msg.equals(other.msg)) {
            return false;
        }
        if (msgType != other.msgType) {
            return false;
        }
        if (nextSend != other.nextSend) {
            return false;
        }
        if (nodeId != other.nodeId) {
            return false;
        }
        if (oldMsg == null) {
            if (other.oldMsg != null) {
                return false;
            }
        } else if (!oldMsg.equals(other.oldMsg)) {
            return false;
        }
        if (retries != other.retries) {
            return false;
        }
        if (revert != other.revert) {
            return false;
        }
        if (subType != other.subType) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MySensorsMessage [" + generateAPIString(this) + "]";
    }

}
