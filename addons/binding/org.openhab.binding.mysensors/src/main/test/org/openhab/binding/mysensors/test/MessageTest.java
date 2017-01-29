/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;

public class MessageTest {

    private static final int OLD_MESSAGE_CUSTOM_HASH[] = { MySensorsMessage.MYSENSORS_MSG_PART_NODE,
            MySensorsMessage.MYSENSORS_MSG_PART_CHILD, MySensorsMessage.MYSENSORS_MSG_PART_TYPE,
            MySensorsMessage.MYSENSORS_MSG_PART_SUBTYPE };

    @Test
    public void testCustomHashCode() {
        MySensorsMessage m1 = new MySensorsMessage(1, 2, MySensorsMessage.MYSENSORS_MSG_TYPE_SET,
                MySensorsMessage.MYSENSORS_ACK_FALSE, false);
        MySensorsMessage m2 = new MySensorsMessage(1, 2, MySensorsMessage.MYSENSORS_MSG_TYPE_SET,
                MySensorsMessage.MYSENSORS_ACK_FALSE, false);

        assertEquals(m2.customHashCode(OLD_MESSAGE_CUSTOM_HASH), m1.customHashCode(OLD_MESSAGE_CUSTOM_HASH));

        m2.setMsg("test");

        assertEquals(m2.customHashCode(OLD_MESSAGE_CUSTOM_HASH), m1.customHashCode(OLD_MESSAGE_CUSTOM_HASH));

        m1.setMsg("test");

        assertEquals(m2.customHashCode(OLD_MESSAGE_CUSTOM_HASH), m1.customHashCode(OLD_MESSAGE_CUSTOM_HASH));

        m1.setAck(MySensorsMessage.MYSENSORS_ACK_TRUE);

        assertEquals(m2.customHashCode(OLD_MESSAGE_CUSTOM_HASH), m1.customHashCode(OLD_MESSAGE_CUSTOM_HASH));

        m2.setRevert(true);

        assertEquals(m2.customHashCode(OLD_MESSAGE_CUSTOM_HASH), m1.customHashCode(OLD_MESSAGE_CUSTOM_HASH));

        m1.setSubType(MySensorsMessage.MYSENSORS_SUBTYPE_S_DOOR);

        assertEquals(m2.customHashCode(OLD_MESSAGE_CUSTOM_HASH), m1.customHashCode(OLD_MESSAGE_CUSTOM_HASH));
    }

}
