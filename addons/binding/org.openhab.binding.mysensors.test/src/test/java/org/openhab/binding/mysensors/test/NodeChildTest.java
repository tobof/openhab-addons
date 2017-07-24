/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.test;

import org.junit.Test;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChildSCustom;

/**
 * Tests for MySensors nodes and child classes.
 * Check if given id is valid according to MySensors specification.
 * 
 * @author Andrea Cioni
 *
 */
public class NodeChildTest {

    @Test(expected = IllegalArgumentException.class)
    public void testWrongChildIdNeg() {
        new MySensorsChildSCustom(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongChildId() {
        new MySensorsChildSCustom(256);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongNodeId255() {
        new MySensorsNode(255);
    }

}
