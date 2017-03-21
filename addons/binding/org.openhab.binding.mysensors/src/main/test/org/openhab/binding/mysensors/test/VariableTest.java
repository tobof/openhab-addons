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

import java.util.Date;

import org.junit.Test;
import org.openhab.binding.mysensors.internal.exception.RevertVariableStateException;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_CUSTOM;

public class VariableTest {

    @Test(expected = RevertVariableStateException.class)
    public void testRevertException1() throws RevertVariableStateException {
        MySensorsVariable v = new MySensorsVariable_V_CUSTOM();
        v.revertValue();
    }

    @Test(expected = RevertVariableStateException.class)
    public void testRevertException2() throws RevertVariableStateException {
        MySensorsVariable v = new MySensorsVariable_V_CUSTOM();

        v.setValue("1");

        v.revertValue();
    }

    @Test
    public void testRevert() throws RevertVariableStateException {
        MySensorsVariable v = new MySensorsVariable_V_CUSTOM();

        v.setValue("1");
        String s = v.getValue();
        Date d = v.getLastUpdate();

        v.setValue("0");

        v.revertValue();

        assertEquals(d, v.getLastUpdate());
        assertEquals(s, v.getValue());
    }

}
