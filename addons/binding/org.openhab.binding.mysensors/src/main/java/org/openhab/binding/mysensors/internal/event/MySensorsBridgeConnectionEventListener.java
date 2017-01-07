/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.event;

import java.util.EventListener;

import org.openhab.binding.mysensors.internal.protocol.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;

/**
 * Handler that implement this interface receive update events from the MySensors network.
 *
 * @author Tim Oberföll
 *
 */
public interface MySensorsBridgeConnectionEventListener extends EventListener {
    /**
     * Procedure to notify new message from MySensorsNetwork.
     */
    default public void messageReceived(MySensorsMessage message) throws Throwable {
    }

    default public void bridgeStatusUpdate(MySensorsBridgeConnection connection, boolean connected)
            throws Throwable {

    }
}
