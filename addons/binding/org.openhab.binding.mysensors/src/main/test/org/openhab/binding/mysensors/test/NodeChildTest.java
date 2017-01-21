package org.openhab.binding.mysensors.test;

import org.junit.Test;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;

public class NodeChildTest {

    @Test(expected = IllegalArgumentException.class)
    public void testWrongChildIdNeg() {
        new MySensorsChild(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongChildId() {
        new MySensorsChild(255);
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
