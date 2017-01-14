/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol.message;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to store the content of a MySensors message.
 *
 * @author Tim Oberföll
 *
 */
public class MySensorsMessage {
    public static String GATEWAY_STARTUP_NOTIFICATION = "Gateway startup complete.";

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

    public MySensorsMessage() {

    }

    public MySensorsMessage(int nodeId, int childId, int msgType, int ack, boolean revert) {
        this.nodeId = nodeId;
        this.childId = childId;
        this.msgType = msgType;
        this.ack = ack;
        this.revert = revert;
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

        if (childId == MYSENSORS_NODE_ID_RESERVED_0 || childId == MYSENSORS_CHILD_ID_RESERVED_255) {
            if (msgType == MYSENSORS_MSG_TYPE_INTERNAL) {
                if (ack == 0) {
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

        if (nodeId == 0) {
            if (childId == MYSENSORS_NODE_ID_RESERVED_0 || childId == MYSENSORS_CHILD_ID_RESERVED_255) {
                if (msgType == MYSENSORS_MSG_TYPE_INTERNAL) {
                    if (ack == 0) {
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

        if (childId == MYSENSORS_NODE_ID_RESERVED_0 || childId == MYSENSORS_CHILD_ID_RESERVED_255) {
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

        if (nodeId == MYSENSORS_NODE_ID_RESERVED_255) {
            if (childId == MYSENSORS_CHILD_ID_RESERVED_255) {
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
