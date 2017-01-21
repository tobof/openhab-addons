package org.openhab.binding.mysensors.test;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;
import org.openhab.binding.mysensors.internal.exception.RevertVariableStateException;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;

public class VariableTest {

    @Test(expected = RevertVariableStateException.class)
    public void testRevertException1() throws RevertVariableStateException {
        MySensorsVariable v = new MySensorsVariable(MySensorsMessage.MYSENSORS_SUBTYPE_V_ARMED);
        v.revertValue();
    }

    @Test(expected = RevertVariableStateException.class)
    public void testRevertException2() throws RevertVariableStateException {
        MySensorsVariable v = new MySensorsVariable(MySensorsMessage.MYSENSORS_SUBTYPE_V_ARMED);

        v.setValue("1");

        v.revertValue();
    }

    @Test
    public void testRevert() throws RevertVariableStateException {
        MySensorsVariable v = new MySensorsVariable(MySensorsMessage.MYSENSORS_SUBTYPE_V_ARMED);

        v.setValue("1");
        String s = v.getValue();
        Date d = v.getLastUpdate();

        v.setValue("0");

        v.revertValue();

        assertEquals(d, v.getLastUpdate());
        assertEquals(s, v.getValue());
    }

}
