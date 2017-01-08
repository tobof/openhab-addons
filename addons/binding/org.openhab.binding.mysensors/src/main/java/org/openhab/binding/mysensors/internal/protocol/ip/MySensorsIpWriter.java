/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol.ip;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.openhab.binding.mysensors.internal.protocol.MySensorsBridgeConnection;

/**
 * Responsible for sending messages to the TCP/IP link.
 *
 * @author Andrea Cioni
 * @author Tim Oberföll
 *
 */
public class MySensorsIpWriter extends MySensorsBridgeConnection.MySensorsWriter {

    public MySensorsIpWriter(Socket sock, MySensorsIpConnection mysCon, int sendDelay) {
        mysCon.super();
        this.mysCon = mysCon;
        try {
            this.outStream = sock.getOutputStream();
            outs = new PrintWriter(outStream);
        } catch (IOException e) {
            logger.error("Exception creating PrintWriter", e);
        }
        this.sendDelay = sendDelay;
    }

}
