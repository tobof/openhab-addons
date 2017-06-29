/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageAck;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessagePart;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageSubType;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageType;

public class MessageTest {

    private static final MySensorsMessagePart OLD_MESSAGE_CUSTOM_HASH[] = { MySensorsMessagePart.NODE,
            MySensorsMessagePart.CHILD, MySensorsMessagePart.TYPE, MySensorsMessagePart.SUBTYPE };

    @Test
    public void testCustomHashCode() {
        MySensorsMessage m1 = new MySensorsMessage(1, 2, MySensorsMessageType.SET, MySensorsMessageAck.FALSE, false);
        MySensorsMessage m2 = new MySensorsMessage(1, 2, MySensorsMessageType.SET, MySensorsMessageAck.FALSE, false);

        assertEquals(m2.customHashCode(OLD_MESSAGE_CUSTOM_HASH), m1.customHashCode(OLD_MESSAGE_CUSTOM_HASH));

        m2.setMsg("test");

        assertEquals(m2.customHashCode(OLD_MESSAGE_CUSTOM_HASH), m1.customHashCode(OLD_MESSAGE_CUSTOM_HASH));

        m1.setMsg("test");

        assertEquals(m2.customHashCode(OLD_MESSAGE_CUSTOM_HASH), m1.customHashCode(OLD_MESSAGE_CUSTOM_HASH));

        m1.setAck(MySensorsMessageAck.TRUE);

        assertEquals(m2.customHashCode(OLD_MESSAGE_CUSTOM_HASH), m1.customHashCode(OLD_MESSAGE_CUSTOM_HASH));

        m2.setRevert(true);

        assertEquals(m2.customHashCode(OLD_MESSAGE_CUSTOM_HASH), m1.customHashCode(OLD_MESSAGE_CUSTOM_HASH));

        m1.setSubType(MySensorsMessageSubType.S_DOOR);

        assertEquals(m2.customHashCode(OLD_MESSAGE_CUSTOM_HASH), m1.customHashCode(OLD_MESSAGE_CUSTOM_HASH));
    }

}
