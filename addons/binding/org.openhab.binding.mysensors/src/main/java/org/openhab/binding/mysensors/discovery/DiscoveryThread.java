/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.discovery;

import org.openhab.binding.mysensors.internal.event.MySensorsEventType;
import org.openhab.binding.mysensors.internal.event.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.internal.event.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.protocol.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;

public class DiscoveryThread implements MySensorsUpdateListener {
    private MySensorsBridgeConnection mysCon;
    private MySensorsDiscoveryService mysDiscoServ;

    public DiscoveryThread(MySensorsBridgeConnection mysCon, MySensorsDiscoveryService mysDiscoServ) {
        this.mysCon = mysCon;
        this.mysDiscoServ = mysDiscoServ;
    }

    public void start() {
        mysCon.addEventListener(this);
    }

    public void stop() {
        mysCon.removeEventListener(this);
    }

    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {
        if (event.getEventType() == MySensorsEventType.INCOMING_MESSAGE) {
            mysDiscoServ.newDevicePresented((MySensorsMessage) event.getData());
        }

    }
}
