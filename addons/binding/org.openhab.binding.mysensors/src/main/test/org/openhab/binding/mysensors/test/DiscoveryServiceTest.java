package org.openhab.binding.mysensors.test;

import static org.junit.Assert.assertEquals;
import static org.openhab.binding.mysensors.MySensorsBindingConstants.THING_UID_MAP;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.junit.Test;

public class DiscoveryServiceTest {

    @Test
    public void testThingUidMap() {
        Set<Integer> keySet = THING_UID_MAP.keySet();

        for (Integer key : keySet) {
            ThingTypeUID thingTypeUid = THING_UID_MAP.get(key);
            // System.out.println(thingTypeUid.getId());
            assertEquals(true, thingTypeUid.getId().matches("^[a-zA-Z-]*$"));
        }
    }

}
