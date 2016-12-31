/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.discovery;

import org.openhab.binding.mysensors.internal.event.MySensorsEventObserver;
import org.openhab.binding.mysensors.internal.event.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;

public class DiscoveryThread implements MySensorsUpdateListener {
    private MySensorsDiscoveryService mysDiscoServ;

    public DiscoveryThread(MySensorsDiscoveryService mysDiscoServ) {
        this.mysDiscoServ = mysDiscoServ;
    }

    public void start() {
        MySensorsEventObserver.addEventListener(this);
    }

    public void stop() {
        MySensorsEventObserver.removeEventListener(this);
    }

    @Override
    public void messageReceived(MySensorsMessage message) throws Throwable {
        mysDiscoServ.newDevicePresented(message);

    }
}
