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
 * @author Tim Oberföll
 *
 *         Used to store the content of a MySensors message
 */
public class MySensorsMessage {
    public static String GATEWAY_STARTUP_NOTIFICATION = "Gateway startup complete.";

    private Logger logger = LoggerFactory.getLogger(MySensorsMessage.class);

    public int nodeId = 0;
    public int childId = 0;
    public int msgType = 0;
    public int ack = 0;
    public boolean revert = true;
    public int subType = 0;
    public String msg = "";
    public String oldMsg = "";
    public int retries = 0;
    public long nextSend = 0;

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

    public void printDebug() {
        logger.debug(String.format("nodeId: %d, childId: %d, msgType: %d, ack: %d, revert: %b, subType: %d ,msg: %s",
                this.nodeId, this.childId, this.msgType, this.ack, this.revert, this.subType, this.msg));
    }

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

    public boolean isIConfigMessage() {
        boolean ret = false;

        if (childId == 0 || childId == 255) {
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

    public boolean isIVersionMessage() {
        boolean ret = false;

        if (nodeId == 0) {
            if (childId == 0 || childId == 255) {
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

    public boolean isITimeMessage() {
        boolean ret = false;

        if (childId == 0 || childId == 255) {
            if (msgType == MYSENSORS_MSG_TYPE_INTERNAL) {
                if (ack == 0) {
                    if (subType == MYSENSORS_SUBTYPE_I_TIME) {
                        ret = true;
                    }
                }
            }
        }

        return ret;
    }

    public boolean isIdRequestMessage() {
        boolean ret = false;

        if (nodeId == 255) {
            if (childId == 255) {
                if (msgType == MYSENSORS_SUBTYPE_I_ID_REQUEST) {
                    if (ack == 0) {
                        if (subType == MYSENSORS_MSG_TYPE_INTERNAL) {
                            ret = true;
                        }
                    }
                }
            }
        }

        return ret;
    }

    public boolean isPresentationMessage() {
        return msgType == MYSENSORS_MSG_TYPE_PRESENTATION;
    }

    public boolean isSetReqMessage() {
        return msgType == MYSENSORS_MSG_TYPE_REQ || msgType == MYSENSORS_MSG_TYPE_SET;
    }

    public boolean isInternalMessage() {
        return msgType == MYSENSORS_MSG_TYPE_INTERNAL;
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
        return "MySensorsMessage [msg=" + msg + "]";
    }

}
