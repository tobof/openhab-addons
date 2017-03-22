/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.converter;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;

/**
 * Used to convert a String from an incoming MySensors message to a StringType
 * 
 * @author Andrea Cioni
 *
 */
public class MySensorsStringTypeConverter implements MySensorsTypeConverter {

    @Override
    public State fromString(String s) {
        return new StringType(s);
    }

}
