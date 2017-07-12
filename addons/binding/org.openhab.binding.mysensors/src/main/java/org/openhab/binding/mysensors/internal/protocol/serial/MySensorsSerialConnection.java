/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol.serial;

import org.openhab.binding.mysensors.internal.event.MySensorsEventRegister;
import org.openhab.binding.mysensors.internal.gateway.MySensorsGatewayConfig;
import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;

import gnu.io.NRSerialPort;

/**
 * Connection to the serial interface where the MySensors Gateway is connected.
 *
 * @author Tim Oberföll
 * @author Andrea Cioni
 *
 */
public class MySensorsSerialConnection extends MySensorsAbstractConnection {

    private NRSerialPort serialConnection = null;

    public MySensorsSerialConnection(MySensorsGatewayConfig myConf, MySensorsEventRegister myEventRegister) {
        super(myConf, myEventRegister);
    }

    /**
     * Tries to accomplish a connection via a serial port to the serial gateway.
     */
    @Override
    public boolean establishConnection() {
        logger.debug("Connecting to {} [baudRate:{}]", myGatewayConfig.getSerialPort(), myGatewayConfig.getBaudRate());

        boolean ret = false;

        serialConnection = new NRSerialPort(myGatewayConfig.getSerialPort(), myGatewayConfig.getBaudRate());
        if (serialConnection.connect()) {
            logger.debug("Successfully connected to serial port.");

            try {
                logger.debug("Waiting {} seconds to allow correct reset trigger on serial connection opening",
                        RESET_TIME_IN_MILLISECONDS / 1000);
                Thread.sleep(RESET_TIME_IN_MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("Interrupted reset time wait");
            }

            mysConReader = new MySensorsReader(serialConnection.getInputStream());
            mysConWriter = new MySensorsWriter(serialConnection.getOutputStream());

            ret = startReaderWriterThread(mysConReader, mysConWriter);
        } else {
            logger.error("Can't connect to serial port. Wrong port?");
        }

        return ret;
    }

    /**
     * Initiates a clean disconnect from the serial gateway.
     */
    @Override
    public void stopConnection() {
        logger.debug("Shutting down serial connection!");

        if (mysConWriter != null) {
            mysConWriter.stopWriting();
            mysConWriter = null;
        }

        if (mysConReader != null) {
            mysConReader.stopReader();
            mysConReader = null;
        }

        if (serialConnection != null) {
            try {
                serialConnection.disconnect();
            } catch (Exception e) {
            }
            serialConnection = null;
        }

    }

    @Override
    public String toString() {
        return "MySensorsSerialConnection [serialPort=" + myGatewayConfig.getSerialPort() + ", baudRate="
                + myGatewayConfig.getBaudRate() + "]";
    }

}
