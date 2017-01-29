package org.openhab.binding.mysensors.test;

import org.junit.Test;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_CUSTOM;

public class NodeChildTest {

    @Test(expected = IllegalArgumentException.class)
    public void testWrongChildIdNeg() {
        new MySensorsChild_S_CUSTOM(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongChildId() {
        new MySensorsChild_S_CUSTOM(255);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongNodeId0() {
        new MySensorsNode(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongNodeId255() {
        new MySensorsNode(255);
    }

}
