/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComNotImpException;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;
import static org.openhab.binding.rfxcom.RFXComValueSelector.DATE_TIME;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 * @since 1.9.0
 */
public class RFXComDateTimeMessageTest {
    @Test
    public void testSomeMessages() throws RFXComException, RFXComNotImpException {
        String hexMessage = "0D580117B90003041D030D150A69";
        byte[] message = DatatypeConverter.parseHexBinary(hexMessage);
        RFXComDateTimeMessage msg = (RFXComDateTimeMessage) RFXComMessageFactory.createMessage(message);
        assertEquals("SubType", RFXComDateTimeMessage.SubType.RTGR328N, msg.subType);
        assertEquals("Seq Number", 23, (short) (msg.seqNbr & 0xFF));
        assertEquals("Sensor Id", "47360", msg.getDeviceId());
        assertEquals("Date time", "2003-04-29T13:21:10", msg.dateTime);
        assertEquals("Signal Level", (byte) 6, msg.signalLevel);

        assertEquals("Converted value", DateTimeType.valueOf("2003-04-29T13:21:10"), msg.convertToState(DATE_TIME));

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMessage, DatatypeConverter.printHexBinary(decoded));
    }
}
