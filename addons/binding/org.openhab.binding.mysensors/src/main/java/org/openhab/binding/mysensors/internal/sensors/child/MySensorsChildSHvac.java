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
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVHvacFlowMode;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVHvacFlowState;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVHvacSetpointCool;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVHvacSetpointHeat;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVHvacSpeed;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVStatus;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariableVTemp;

/**
 * MySensors Child definition according to MySensors serial API
 * https://www.mysensors.org/download/serial_api_20
 * 
 * @author Andrea Cioni
 * @author Tim Oberföll
 *
 */
public class MySensorsChildSHvac extends MySensorsChild {

    public MySensorsChildSHvac(int childId) {
        super(childId);
        setPresentationCode(MySensorsMessage.MYSENSORS_SUBTYPE_S_HVAC);
        try {
            addVariable(new MySensorsVariableVStatus());
            addVariable(new MySensorsVariableVTemp());
            addVariable(new MySensorsVariableVHvacSetpointHeat());
            addVariable(new MySensorsVariableVHvacSetpointCool());
            addVariable(new MySensorsVariableVHvacFlowState());
            addVariable(new MySensorsVariableVHvacFlowMode());
            addVariable(new MySensorsVariableVHvacSpeed());
        } catch (NoContentException e) {
            logger.debug("No content to add: {}", e.toString());
        }
    }

}
