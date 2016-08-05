package org.openhab.binding.mysensors;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import org.openhab.binding.mysensors.internal.MySensorsMessage;

public class MySensorsBindingUtility {
    public static boolean isIConfigMessage(MySensorsMessage msg) {
        boolean ret = false;

        if (msg != null) {
            if (msg.getChildId() == 0 || msg.getChildId() == 255) {
                if (msg.getMsgType() == MYSENSORS_MSG_TYPE_INTERNAL) {
                    if (msg.getAck() == 0) {
                        if (msg.getSubType() == MYSENSORS_SUBTYPE_I_CONFIG) {
                            ret = true;
                        }
                    }
                }
            }
        }

        return ret;
    }

    public static boolean isIVersionMessage(MySensorsMessage msg) {
        boolean ret = false;

        if (msg.getNodeId() == 0) {
            if (msg.getChildId() == 0 || msg.getChildId() == 255) {
                if (msg.getMsgType() == MYSENSORS_MSG_TYPE_INTERNAL) {
                    if (msg.getAck() == 0) {
                        if (msg.getSubType() == MYSENSORS_SUBTYPE_I_VERSION) {
                            ret = true;
                        }
                    }
                }
            }
        }

        return ret;
    }

    public static boolean isITimeMessage(MySensorsMessage msg) {
        boolean ret = false;

        if (msg.getChildId() == 0 || msg.getChildId() == 255) {
            if (msg.getMsgType() == MYSENSORS_MSG_TYPE_INTERNAL) {
                if (msg.getAck() == 0) {
                    if (msg.getSubType() == MYSENSORS_SUBTYPE_I_TIME) {
                        ret = true;
                    }
                }
            }
        }

        return ret;
    }
}
