/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the clock command class.
 *
 * @author Chris Jackson
 */
@XStreamAlias("clockCommandClass")
public class ZWaveClockCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveClockCommandClass.class);

    private static final int CLOCK_SET = 4;
    private static final int CLOCK_GET = 5;
    private static final int CLOCK_REPORT = 6;

    /**
     * Creates a new instance of the ZWaveClockCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveClockCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.CLOCK;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received clock command (v{})", this.getNode().getNodeId(), this.getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            default:
                logger.warn(String.format("NODE %d: Unsupported Command %d for command class %s (0x%02X).",
                        this.getNode().getNodeId(), command, this.getCommandClass().getLabel(),
                        this.getCommandClass().getKey()));
        }
    }
}
