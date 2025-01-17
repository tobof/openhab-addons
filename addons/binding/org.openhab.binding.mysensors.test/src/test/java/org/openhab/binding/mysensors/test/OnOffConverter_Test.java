/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mysensors.test;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.junit.Test;
import org.openhab.binding.mysensors.converter.MySensorsOnOffTypeConverter;

/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
public class OnOffConverter_Test {
    @Test
    public void testFromString() {
        MySensorsOnOffTypeConverter onOffConverter = new MySensorsOnOffTypeConverter();
        
        assertEquals(OnOffType.OFF, onOffConverter.fromString("0"));
        assertEquals(OnOffType.ON, onOffConverter.fromString("1"));
        
    }
}
