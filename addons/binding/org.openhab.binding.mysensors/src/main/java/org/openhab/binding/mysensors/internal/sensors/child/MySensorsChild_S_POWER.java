/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.sensors.child;

import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_KWH;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_POWER_FACTOR;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_VA;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_VAR;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_WATT;

public class MySensorsChild_S_POWER extends MySensorsChild {

    public MySensorsChild_S_POWER(int childId) {
        super(childId);
        setPresentationCode(MySensorsMessage.MYSENSORS_SUBTYPE_S_POWER);
        addVariable(new MySensorsVariable_V_WATT());
        addVariable(new MySensorsVariable_V_KWH());
        addVariable(new MySensorsVariable_V_VAR());
        addVariable(new MySensorsVariable_V_VA());
        addVariable(new MySensorsVariable_V_POWER_FACTOR());
    }

}
