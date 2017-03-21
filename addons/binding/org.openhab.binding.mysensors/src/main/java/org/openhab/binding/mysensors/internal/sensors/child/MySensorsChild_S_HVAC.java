/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_HVAC_FLOW_MODE;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_HVAC_FLOW_STATE;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_HVAC_SETPOINT_COOL;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_HVAC_SETPOINT_HEAT;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_HVAC_SPEED;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_STATUS;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_TEMP;

public class MySensorsChild_S_HVAC extends MySensorsChild {

    public MySensorsChild_S_HVAC(int childId) {
        super(childId);
        setPresentationCode(MySensorsMessage.MYSENSORS_SUBTYPE_S_HVAC);
        addVariable(new MySensorsVariable_V_STATUS());
        addVariable(new MySensorsVariable_V_TEMP());
        addVariable(new MySensorsVariable_V_HVAC_SETPOINT_HEAT());
        addVariable(new MySensorsVariable_V_HVAC_SETPOINT_COOL());
        addVariable(new MySensorsVariable_V_HVAC_FLOW_STATE());
        addVariable(new MySensorsVariable_V_HVAC_FLOW_MODE());
        addVariable(new MySensorsVariable_V_HVAC_SPEED());
    }

}
