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
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_IR_RECEIVE;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_IR_RECORD;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_IR_SEND;

public class MySensorsChild_S_IR extends MySensorsChild {

    public MySensorsChild_S_IR(int childId) {
        super(childId);
        setPresentationCode(MySensorsMessage.MYSENSORS_SUBTYPE_S_IR);
        addVariable(new MySensorsVariable_V_IR_SEND());
        addVariable(new MySensorsVariable_V_IR_RECEIVE());
        addVariable(new MySensorsVariable_V_IR_RECORD());
    }

}
