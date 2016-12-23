/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.model.param;

/**
 * Parameter type enumeration. Contains the ISS API parameter key string.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public enum ParamType {

    GENERIC_VALUE("Value"),
    TEMPERATURE_VALUE("Value"),
    TEMPERATURE_DUAL("temp"),
    LUMINOSITY_VALUE("Value"),
    HYGROMETRY_DUAL("hygro"),
    HYGROMETRY_VALUE("Value"),
    CO2_VALUE("Value"),
    PRESSURE_VALUE("Value"),
    NOISE_VALUE("Value"),
    RAIN_VALUE("Value"),
    UV_VALUE("Value"),
    DIMMABLE("dimmable"),
    ENERGY("Energy"),
    STATUS("Status"),
    MULTISWITCH_VALUE("Value"),
    CHOICES("Choices"),
    COLOR("color"),
    LEVEL("Level"),
    WHITE_CHANNEL("whitechannel"),
    WATTS("Watts"),
    KWH("ConsoTotal"),
    ARMABLE("armable"),
    ARMED("Armed"),
    ACKABLE("ackable"),
    TRIPPED("Tripped"),
    LAST_TRIP("lasttrip"),
    ACCUMULATION("Accumulation"),
    SPEED("Speed"),
    DIRECTION("Direction");

    private final String apiString;

    ParamType(String apiString) {
        this.apiString = apiString;
    }

    public String getApiString() {
        return apiString;
    }

}
