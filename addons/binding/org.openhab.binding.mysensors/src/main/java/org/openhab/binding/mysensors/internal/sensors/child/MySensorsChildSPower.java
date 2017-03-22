/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.exception.NoContentException;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVKwh;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVPowerFactor;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVVa;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVVar;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVWatt;

/**
 * MySensors Child definition according to MySensors serial API
 * https://www.mysensors.org/download/serial_api_20
 * 
 * @author Andrea Cioni
 * @author Tim Oberföll
 *
 */
public class MySensorsChildSPower extends MySensorsChild {

    public MySensorsChildSPower(int childId) {
        super(childId);
        setPresentationCode(MySensorsMessage.MYSENSORS_SUBTYPE_S_POWER);
        try {
            addVariable(new MySensorsVariableVWatt());
            addVariable(new MySensorsVariableVKwh());
            addVariable(new MySensorsVariableVVar());
            addVariable(new MySensorsVariableVVa());
            addVariable(new MySensorsVariableVPowerFactor());
        } catch (NoContentException e) {
            logger.debug("No content to add: {}", e.toString());
        }
    }

}
