/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.discovery;

import org.openhab.binding.mysensors.internal.event.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.protocol.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;

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
    public void messageReceived(MySensorsMessage message) throws Throwable {
        mysDiscoServ.newDevicePresented(message);

    }

    @Override
    public void nodeIdReservationDone(Integer reservedId) throws Throwable {
        // TODO Auto-generated method stub

    }

    @Override
    public void newNodeDiscovered(MySensorsNode message) throws Throwable {
        // TODO Auto-generated method stub

    }

    @Override
    public void nodeUpdateEvent(MySensorsNode node, MySensorsChild child, MySensorsVariable var) {
        // TODO Auto-generated method stub

    }

    @Override
    public void nodeReachStatusChanged(MySensorsNode node, boolean reach) {
        // TODO Auto-generated method stub

    }

    @Override
    public void bridgeStatusUpdate(MySensorsBridgeConnection connection, boolean connected) throws Throwable {
        // TODO Auto-generated method stub

    }
}
