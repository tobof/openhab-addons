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
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_LEVEL;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_UNIT_PREFIX;

public class MySensorsChild_S_AIR_QUALITY extends MySensorsChild {

    public MySensorsChild_S_AIR_QUALITY(int childId) {
        super(childId);
        setPresentationCode(MySensorsMessage.MYSENSORS_SUBTYPE_S_AIR_QUALITY);
        addVariable(new MySensorsVariable_V_LEVEL());
        addVariable(new MySensorsVariable_V_UNIT_PREFIX());
    }

}
