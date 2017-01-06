/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.junit.Test;
import org.openhab.binding.rfxcom.RFXComValueSelector;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComNotImpException;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.assertEquals;
import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.LIGHTING5;
import static org.openhab.binding.rfxcom.internal.messages.RFXComLighting5Message.Commands.ON;
import static org.openhab.binding.rfxcom.internal.messages.RFXComLighting5Message.SubType.IT;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 * @since 1.9.0
 */
public class RFXComLighting5MessageTest {
    @Test
    public void basicBoundaryCheck() throws RFXComException, RFXComNotImpException {
        RFXComTestHelper.basicBoundaryCheck(LIGHTING5);
    }

    @Test
    public void convertFromStateItMessage() throws RFXComException, RFXComNotImpException {
        RFXComMessage itMessageObject = RFXComMessageFactory.createMessage(LIGHTING5);
        itMessageObject.setDeviceId("2061.1");
        itMessageObject.setSubType(IT);
        itMessageObject.convertFromState(RFXComValueSelector.COMMAND, OnOffType.ON);
        byte[] message = itMessageObject.decodeMessage();
        String hexMessage = DatatypeConverter.printHexBinary(message);
        assertEquals("Message is not as expected", "0A140F0000080D01010000", hexMessage);
        RFXComLighting5Message msg = (RFXComLighting5Message) RFXComMessageFactory.createMessage(message);
        assertEquals("SubType", IT, msg.subType);
        assertEquals("Sensor Id", "2061.1", msg.getDeviceId());
        assertEquals("Command", ON, msg.command);
    }

    // TODO please add more tests for different messages
}
