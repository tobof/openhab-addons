/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol.serial;

import java.io.OutputStream;
import java.io.PrintWriter;

import org.openhab.binding.mysensors.internal.protocol.MySensorsWriter;

/**
 * Responsible for sending messages to the serial link.
 *
 * @author Andrea Cioni
 * @author Tim Oberföll
 *
 */
public class MySensorsSerialWriter extends MySensorsWriter {

    public MySensorsSerialWriter(OutputStream outStream, MySensorsSerialConnection mysCon, int sendDelay) {
        this.mysCon = mysCon;
        this.outStream = outStream;
        outs = new PrintWriter(outStream);
        this.sendDelay = sendDelay;
    }
}
