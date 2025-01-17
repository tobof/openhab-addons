/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mysensors.internal.sensors.variable;

import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageSubType;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;

/**
 * MySensors variable definition according to MySensors serial API
 * https://www.mysensors.org/download/serial_api_20
 *
 * @author Andrea Cioni
 * @author Tim Oberföll
 *
 */
public class MySensorsVariableVSceneOn extends MySensorsVariable {

    public MySensorsVariableVSceneOn() {
        super(MySensorsMessageSubType.V_SCENE_ON);
    }
}
