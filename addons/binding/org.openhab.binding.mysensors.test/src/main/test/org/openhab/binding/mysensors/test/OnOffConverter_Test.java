package org.openhab.binding.mysensors.test;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.junit.Test;
import org.openhab.binding.mysensors.converter.MySensorsOnOffTypeConverter;

public class OnOffConverter_Test {
    @Test
    public void testFromString() {
        MySensorsOnOffTypeConverter onOffConverter = new MySensorsOnOffTypeConverter();
        
        assertEquals(OnOffType.OFF, onOffConverter.fromString("0"));
        assertEquals(OnOffType.ON, onOffConverter.fromString("1"));
        
    }
}
