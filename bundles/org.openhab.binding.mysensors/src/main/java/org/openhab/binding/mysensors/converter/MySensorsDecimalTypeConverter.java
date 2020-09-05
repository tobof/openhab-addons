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
package org.openhab.binding.mysensors.converter;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;

/**
 * Used to convert a String from an incoming MySensors message to a DecimalType
 *
 * @author Andrea Cioni - Initial contribution
 *
 */
@NonNullByDefault
public class MySensorsDecimalTypeConverter implements MySensorsTypeConverter {

    @Override
    public State fromString(@NonNull String string) {
        return new DecimalType(string);
    }
}