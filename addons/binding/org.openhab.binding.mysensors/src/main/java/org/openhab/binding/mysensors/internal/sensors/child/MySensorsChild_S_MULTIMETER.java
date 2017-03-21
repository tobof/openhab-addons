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
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_CURRENT;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_IMPEDANCE;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_VOLTAGE;

public class MySensorsChild_S_MULTIMETER extends MySensorsChild {

    public MySensorsChild_S_MULTIMETER(int childId) {
        super(childId);
        setPresentationCode(MySensorsMessage.MYSENSORS_SUBTYPE_S_MULTIMETER);
        addVariable(new MySensorsVariable_V_VOLTAGE());
        addVariable(new MySensorsVariable_V_CURRENT());
        addVariable(new MySensorsVariable_V_IMPEDANCE());
    }

}
