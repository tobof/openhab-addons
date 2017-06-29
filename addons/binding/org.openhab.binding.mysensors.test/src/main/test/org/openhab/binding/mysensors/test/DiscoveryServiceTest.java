/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.test;

import static org.junit.Assert.assertEquals;
import static org.openhab.binding.mysensors.MySensorsBindingConstants.THING_UID_MAP;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.junit.Test;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageSubType;

public class DiscoveryServiceTest {

    @Test
    public void testThingUidMap() {
        Set<MySensorsMessageSubType> keySet = THING_UID_MAP.keySet();

        for (MySensorsMessageSubType key : keySet) {
            ThingTypeUID thingTypeUid = THING_UID_MAP.get(key);
            // System.out.println(thingTypeUid.getId());
            assertEquals(true, thingTypeUid.getId().matches("^[a-zA-Z-]*$"));
        }
    }

}
