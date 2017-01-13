/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.discovery;

import org.openhab.binding.mysensors.internal.event.MySensorsGatewayEventListener;
import org.openhab.binding.mysensors.internal.gateway.MySensorsGateway;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;

/**
 * Thread is started for discovery of new things / nodes.
 *
 * @author Tim Oberföll
 * @author Andrea Cioni
 *
 */
public class DiscoveryThread implements MySensorsGatewayEventListener {

    private MySensorsGateway myGateway;

    private MySensorsDiscoveryService mysDiscoServ;

    /**
     * Initialize the discovery thread.
     *
     * @param mysCon Location of the EventListener to receive messages from the MySensors network.
     * @param mysDiscoServ Location of the service that started this thread
     */
    public DiscoveryThread(MySensorsGateway myGateway, MySensorsDiscoveryService mysDiscoServ) {
        this.myGateway = myGateway;
        this.mysDiscoServ = mysDiscoServ;
    }

    /**
     * Start the discovery process.
     */
    public void start() {
        myGateway.getEventRegister().addEventListener(this);
    }

    /**
     * Stop the discovery process.
     */
    public void stop() {
        myGateway.getEventRegister().removeEventListener(this);
    }

    @Override
    public void messageReceived(MySensorsMessage message) throws Throwable {
        mysDiscoServ.newDevicePresented(message);

    }
}
