/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors;

import static org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.mysensors.adapter.MySensorsDecimalTypeAdapter;
import org.openhab.binding.mysensors.adapter.MySensorsOnOffTypeAdapter;
import org.openhab.binding.mysensors.adapter.MySensorsOpenCloseTypeAdapter;
import org.openhab.binding.mysensors.adapter.MySensorsPercentTypeAdapter;
import org.openhab.binding.mysensors.adapter.MySensorsStringTypeAdapter;
import org.openhab.binding.mysensors.adapter.MySensorsTypeAdapter;
import org.openhab.binding.mysensors.adapter.MySensorsUpDownTypeAdapter;
import org.openhab.binding.mysensors.internal.MySensorsUtility;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * The {@link MySensorsBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tim Oberföll
 */
public class MySensorsBindingConstants {

    public static final String BINDING_ID = "mysensors";

    // parameters / fields of a MySensors message
    public static final String PARAMETER_NODEID = "nodeId";
    public static final String PARAMETER_CHILDID = "childId";
    public static final String PARAMETER_IPADDRESS = "ipAddress";
    public static final String PRAMETER_TCPPORT = "tcpPort";
    public static final String PARAMETER_SENDDELAY = "sendDelay";
    public static final String PARAMETER_BAUDRATE = "baudRate";
    public static final String PARAMETER_REQUESTACK = "requestack";

    /**
     * All knowing thing. A node with nodeId 999 and childId 999 receives all messages
     * received from the MySensors bridge/gateway. Useful for debugging and for implementation
     * of features not covered by the binding (for example with rules)
     */
    public static final int MYSENSORS_NODE_ID_ALL_KNOWING = 999;
    public static final int MYSENSORS_CHILD_ID_ALL_KNOWING = 999;

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_HUMIDITY = new ThingTypeUID(BINDING_ID, "humidity");
    public final static ThingTypeUID THING_TYPE_TEMPERATURE = new ThingTypeUID(BINDING_ID, "temperature");
    public final static ThingTypeUID THING_TYPE_MULTIMETER = new ThingTypeUID(BINDING_ID, "multimeter");
    public final static ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "light");
    public final static ThingTypeUID THING_TYPE_POWER = new ThingTypeUID(BINDING_ID, "power");
    public final static ThingTypeUID THING_TYPE_BARO = new ThingTypeUID(BINDING_ID, "baro");
    public final static ThingTypeUID THING_TYPE_DOOR = new ThingTypeUID(BINDING_ID, "door");
    public final static ThingTypeUID THING_TYPE_MOTION = new ThingTypeUID(BINDING_ID, "motion");
    public final static ThingTypeUID THING_TYPE_SMOKE = new ThingTypeUID(BINDING_ID, "smoke");
    public final static ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public final static ThingTypeUID THING_TYPE_COVER = new ThingTypeUID(BINDING_ID, "cover");
    public final static ThingTypeUID THING_TYPE_WIND = new ThingTypeUID(BINDING_ID, "wind");
    public final static ThingTypeUID THING_TYPE_RAIN = new ThingTypeUID(BINDING_ID, "rain");
    public final static ThingTypeUID THING_TYPE_UV = new ThingTypeUID(BINDING_ID, "uv");
    public final static ThingTypeUID THING_TYPE_WEIGHT = new ThingTypeUID(BINDING_ID, "weight");
    public final static ThingTypeUID THING_TYPE_DISTANCE = new ThingTypeUID(BINDING_ID, "distance");
    public final static ThingTypeUID THING_TYPE_LIGHT_LEVEL = new ThingTypeUID(BINDING_ID, "light-level");
    public final static ThingTypeUID THING_TYPE_WATER = new ThingTypeUID(BINDING_ID, "waterMeter");
    public final static ThingTypeUID THING_TYPE_CUSTOM = new ThingTypeUID(BINDING_ID, "customSensor");
    public final static ThingTypeUID THING_TYPE_HVAC = new ThingTypeUID(BINDING_ID, "hvacThermostat");
    public final static ThingTypeUID THING_TYPE_LOCK = new ThingTypeUID(BINDING_ID, "lock");
    public final static ThingTypeUID THING_TYPE_SOUND = new ThingTypeUID(BINDING_ID, "sound");
    public final static ThingTypeUID THING_TYPE_RGB_LIGHT = new ThingTypeUID(BINDING_ID, "rgbLight");
    public final static ThingTypeUID THING_TYPE_RGBW_LIGHT = new ThingTypeUID(BINDING_ID, "rgbwLight");
    public final static ThingTypeUID THING_TYPE_WATER_QUALITY = new ThingTypeUID(BINDING_ID, "waterQuality");
    public final static ThingTypeUID THING_TYPE_MYSENSORS_MESSAGE = new ThingTypeUID(BINDING_ID, "mySensorsMessage");
    public final static ThingTypeUID THING_TYPE_TEXT = new ThingTypeUID(BINDING_ID, "text");
    public final static ThingTypeUID THING_TYPE_IR_SEND = new ThingTypeUID(BINDING_ID, "irSend");
    public final static ThingTypeUID THING_TYPE_IR_RECEIVE = new ThingTypeUID(BINDING_ID, "irReceive");
    public final static ThingTypeUID THING_TYPE_AIR_QUALITY = new ThingTypeUID(BINDING_ID, "airQuality");

    // List of bridges
    public final static ThingTypeUID THING_TYPE_BRIDGE_SER = new ThingTypeUID(BINDING_ID, "bridge-ser");
    public final static ThingTypeUID THING_TYPE_BRIDGE_ETH = new ThingTypeUID(BINDING_ID, "bridge-eth");

    // List of all Channel ids
    public final static String CHANNEL_HUM = "hum";
    public final static String CHANNEL_TEMP = "temp";
    public final static String CHANNEL_VOLT = "volt";
    public final static String CHANNEL_WATT = "watt";
    public final static String CHANNEL_KWH = "kwh";
    public final static String CHANNEL_STATUS = "status";
    public final static String CHANNEL_PRESSURE = "pressure";
    public final static String CHANNEL_BARO = "baro";
    public final static String CHANNEL_TRIPPED = "tripped";
    public final static String CHANNEL_ARMED = "armed";
    public final static String CHANNEL_DIMMER = "dimmer";
    public final static String CHANNEL_COVER = "cover";
    public final static String CHANNEL_WIND = "wind";
    public final static String CHANNEL_GUST = "gust";
    public final static String CHANNEL_RAIN = "rain";
    public final static String CHANNEL_RAINRATE = "rainrate";
    public final static String CHANNEL_UV = "uv";
    public final static String CHANNEL_WEIGHT = "weight";
    public final static String CHANNEL_IMPEDANCE = "impedance";
    public final static String CHANNEL_CURRENT = "current";
    public final static String CHANNEL_DISTANCE = "distance";
    public final static String CHANNEL_LIGHT_LEVEL = "light-level";
    public final static String CHANNEL_VERSION = "version";
    public final static String CHANNEL_BATTERY = "battery";
    public final static String CHANNEL_HVAC_FLOW_STATE = "hvac-flow-state";
    public final static String CHANNEL_HVAC_FLOW_MODE = "hvac-flow-mode";
    public final static String CHANNEL_HVAC_SETPOINT_HEAT = "hvac-setPoint-heat";
    public final static String CHANNEL_HVAC_SETPOINT_COOL = "hvac-setPoint-cool";
    public final static String CHANNEL_HVAC_SPEED = "hvac-speed";
    public final static String CHANNEL_VAR1 = "var1";
    public final static String CHANNEL_VAR2 = "var2";
    public final static String CHANNEL_VAR3 = "var3";
    public final static String CHANNEL_VAR4 = "var4";
    public final static String CHANNEL_VAR5 = "var5";
    public final static String CHANNEL_FLOW = "flow";
    public final static String CHANNEL_VOLUME = "volume";
    public final static String CHANNEL_LOCK_STATUS = "lock-status";
    public final static String CHANNEL_LEVEL = "level";
    public final static String CHANNEL_RGB = "rgb";
    public final static String CHANNEL_RGBW = "rgbw";
    public final static String CHANNEL_ID = "id";
    public final static String CHANNEL_UNIT_PREFIX = "unit-prefix";
    public final static String CHANNEL_TEXT = "text";
    public final static String CHANNEL_CUSTOM = "custom";
    public final static String CHANNEL_POSITION = "position";
    public final static String CHANNEL_IR_RECORD = "ir-record";
    public final static String CHANNEL_PH = "ph";
    public final static String CHANNEL_ORP = "orp";
    public final static String CHANNEL_EC = "ec";
    public final static String CHANNEL_VAR = "var";
    public final static String CHANNEL_VA = "va";
    public final static String CHANNEL_POWER_FACTOR = "power-factor";
    public final static String CHANNEL_IR_SEND = "irSend";
    public final static String CHANNEL_IR_RECEIVE = "irReceive";
    public final static String CHANNEL_CO2_LEVEL = "co2-level";

    // Extra channel names for non-standard MySensors channels
    public final static String CHANNEL_MYSENSORS_MESSAGE = "mySensorsMessage";
    public final static String CHANNEL_LAST_UPDATE = "lastupdate";

    /**
     * Mapping MySensors message type/subtypes to channels.
     */
    public final static Map<Integer, String> CHANNEL_MAP = new HashMap<Integer, String>() {
        /**
         *
         */
        private static final long serialVersionUID = -7970323220036599380L;

        {
            put(MYSENSORS_SUBTYPE_V_TEMP, CHANNEL_TEMP);
            put(MYSENSORS_SUBTYPE_V_HUM, CHANNEL_HUM);
            put(MYSENSORS_SUBTYPE_V_STATUS, CHANNEL_STATUS);
            put(MYSENSORS_SUBTYPE_V_VOLTAGE, CHANNEL_VOLT);
            put(MYSENSORS_SUBTYPE_V_WATT, CHANNEL_WATT);
            put(MYSENSORS_SUBTYPE_V_KWH, CHANNEL_KWH);
            put(MYSENSORS_SUBTYPE_V_PRESSURE, CHANNEL_PRESSURE);
            put(MYSENSORS_SUBTYPE_V_FORECAST, CHANNEL_BARO);
            put(MYSENSORS_SUBTYPE_V_TRIPPED, CHANNEL_TRIPPED);
            put(MYSENSORS_SUBTYPE_V_ARMED, CHANNEL_ARMED);
            put(MYSENSORS_SUBTYPE_V_PERCENTAGE, CHANNEL_DIMMER);
            put(MYSENSORS_SUBTYPE_V_UP, CHANNEL_COVER);
            put(MYSENSORS_SUBTYPE_V_DOWN, CHANNEL_COVER);
            put(MYSENSORS_SUBTYPE_V_STOP, CHANNEL_COVER);
            put(MYSENSORS_SUBTYPE_V_WIND, CHANNEL_WIND);
            put(MYSENSORS_SUBTYPE_V_GUST, CHANNEL_GUST);
            put(MYSENSORS_SUBTYPE_V_RAIN, CHANNEL_RAIN);
            put(MYSENSORS_SUBTYPE_V_RAINRATE, CHANNEL_RAINRATE);
            put(MYSENSORS_SUBTYPE_V_UV, CHANNEL_UV);
            put(MYSENSORS_SUBTYPE_V_WEIGHT, CHANNEL_WEIGHT);
            put(MYSENSORS_SUBTYPE_V_IMPEDANCE, CHANNEL_IMPEDANCE);
            put(MYSENSORS_SUBTYPE_V_DISTANCE, CHANNEL_DISTANCE);
            put(MYSENSORS_SUBTYPE_V_LIGHT_LEVEL, CHANNEL_LIGHT_LEVEL);
            put(MYSENSORS_SUBTYPE_V_CURRENT, CHANNEL_CURRENT);
            put(MYSENSORS_SUBTYPE_V_HVAC_FLOW_STATE, CHANNEL_HVAC_FLOW_STATE);
            put(MYSENSORS_SUBTYPE_V_HVAC_SPEED, CHANNEL_HVAC_SPEED);
            put(MYSENSORS_SUBTYPE_V_HVAC_SETPOINT_COOL, CHANNEL_HVAC_SETPOINT_COOL);
            put(MYSENSORS_SUBTYPE_V_HVAC_SETPOINT_HEAT, CHANNEL_HVAC_SETPOINT_HEAT);
            put(MYSENSORS_SUBTYPE_V_HVAC_FLOW_MODE, CHANNEL_HVAC_FLOW_MODE);
            put(MYSENSORS_SUBTYPE_V_VAR1, CHANNEL_VAR1);
            put(MYSENSORS_SUBTYPE_V_VAR2, CHANNEL_VAR2);
            put(MYSENSORS_SUBTYPE_V_VAR3, CHANNEL_VAR3);
            put(MYSENSORS_SUBTYPE_V_VAR4, CHANNEL_VAR4);
            put(MYSENSORS_SUBTYPE_V_VAR5, CHANNEL_VAR5);
            put(MYSENSORS_SUBTYPE_V_FLOW, CHANNEL_FLOW);
            put(MYSENSORS_SUBTYPE_V_VOLUME, CHANNEL_VOLUME);
            put(MYSENSORS_SUBTYPE_V_LOCK_STATUS, CHANNEL_LOCK_STATUS);
            put(MYSENSORS_SUBTYPE_V_LEVEL, CHANNEL_LEVEL);
            put(MYSENSORS_SUBTYPE_V_LEVEL, CHANNEL_CO2_LEVEL); // FIXME
            put(MYSENSORS_SUBTYPE_V_RGB, CHANNEL_RGB);
            put(MYSENSORS_SUBTYPE_V_RGBW, CHANNEL_RGBW);
            put(MYSENSORS_SUBTYPE_V_ID, CHANNEL_ID);
            put(MYSENSORS_SUBTYPE_V_UNIT_PREFIX, CHANNEL_UNIT_PREFIX);
            put(MYSENSORS_SUBTYPE_V_TEXT, CHANNEL_TEXT);
            put(MYSENSORS_SUBTYPE_V_CUSTOM, CHANNEL_CUSTOM);
            put(MYSENSORS_SUBTYPE_V_POSITION, CHANNEL_POSITION);
            put(MYSENSORS_SUBTYPE_V_IR_RECORD, CHANNEL_IR_RECORD);
            put(MYSENSORS_SUBTYPE_V_PH, CHANNEL_PH);
            put(MYSENSORS_SUBTYPE_V_ORP, CHANNEL_ORP);
            put(MYSENSORS_SUBTYPE_V_EC, CHANNEL_EC);
            put(MYSENSORS_SUBTYPE_V_VAR, CHANNEL_VAR);
            put(MYSENSORS_SUBTYPE_V_VA, CHANNEL_VA);
            put(MYSENSORS_SUBTYPE_V_POWER_FACTOR, CHANNEL_POWER_FACTOR);
            put(MYSENSORS_SUBTYPE_V_TEXT, CHANNEL_TEXT);
            put(MYSENSORS_SUBTYPE_V_IR_SEND, CHANNEL_IR_SEND);
            put(MYSENSORS_SUBTYPE_V_IR_RECEIVE, CHANNEL_IR_RECEIVE);
        }
    };

    /**
     * Inverse of the CHANNEL_MAP, duplicate allowed (see also Converters here below)
     */
    public final static Map<String, Integer> INVERSE_CHANNEL_MAP = MySensorsUtility.invertMap(CHANNEL_MAP, true);

    /**
     * Converters will be used to map values from OH to/from MySensors Variables
     */
    public static final MySensorsDecimalTypeAdapter DECIMAL_TYPE_CONVERTER = new MySensorsDecimalTypeAdapter();
    public static final MySensorsPercentTypeAdapter PERCENT_TYPE_CONVERTER = new MySensorsPercentTypeAdapter();
    public static final MySensorsOnOffTypeAdapter ONOFF_TYPE_CONVERTER = new MySensorsOnOffTypeAdapter();
    public static final MySensorsOpenCloseTypeAdapter OPENCLOSE_TYPE_CONVERTER = new MySensorsOpenCloseTypeAdapter();
    public static final MySensorsUpDownTypeAdapter UPDOWN_TYPE_CONVERTER = new MySensorsUpDownTypeAdapter();
    public static final MySensorsStringTypeAdapter STRING_TYPE_CONVERTER = new MySensorsStringTypeAdapter();

    /**
     * Mappings between ChannelUID and class that represents the type of the channel
     */
    public final static Map<String, MySensorsTypeAdapter> TYPE_MAP = new HashMap<String, MySensorsTypeAdapter>() {

        /**
         *
         */
        private static final long serialVersionUID = 6273187523631143905L;
        {
            put(CHANNEL_TEMP, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_HUM, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_STATUS, ONOFF_TYPE_CONVERTER);
            put(CHANNEL_VOLT, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_WATT, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_KWH, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_PRESSURE, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_BARO, STRING_TYPE_CONVERTER);
            put(CHANNEL_TRIPPED, OPENCLOSE_TYPE_CONVERTER);
            put(CHANNEL_ARMED, OPENCLOSE_TYPE_CONVERTER);
            put(CHANNEL_DIMMER, PERCENT_TYPE_CONVERTER);
            put(CHANNEL_COVER, UPDOWN_TYPE_CONVERTER);
            put(CHANNEL_COVER, UPDOWN_TYPE_CONVERTER); // !
            put(CHANNEL_COVER, UPDOWN_TYPE_CONVERTER); // !
            put(CHANNEL_WIND, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_GUST, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_RAIN, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_RAINRATE, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_UV, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_WEIGHT, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_IMPEDANCE, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_DISTANCE, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_LIGHT_LEVEL, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_CURRENT, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_HVAC_FLOW_STATE, STRING_TYPE_CONVERTER);
            put(CHANNEL_HVAC_SPEED, STRING_TYPE_CONVERTER);
            put(CHANNEL_HVAC_SETPOINT_COOL, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_HVAC_SETPOINT_HEAT, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_HVAC_FLOW_MODE, STRING_TYPE_CONVERTER);
            put(CHANNEL_VAR1, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_VAR2, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_VAR3, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_VAR4, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_VAR5, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_FLOW, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_VOLUME, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_LOCK_STATUS, OPENCLOSE_TYPE_CONVERTER);
            put(CHANNEL_LEVEL, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_CO2_LEVEL, DECIMAL_TYPE_CONVERTER); // FIXME
            put(CHANNEL_RGB, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_RGBW, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_ID, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_UNIT_PREFIX, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_TEXT, STRING_TYPE_CONVERTER);
            put(CHANNEL_CUSTOM, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_POSITION, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_IR_RECORD, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_PH, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_ORP, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_EC, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_VAR, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_VA, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_POWER_FACTOR, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_TEXT, STRING_TYPE_CONVERTER);
            put(CHANNEL_IR_SEND, STRING_TYPE_CONVERTER);
            put(CHANNEL_IR_RECEIVE, STRING_TYPE_CONVERTER);

            // Internal
            put(CHANNEL_VERSION, STRING_TYPE_CONVERTER);
            put(CHANNEL_BATTERY, DECIMAL_TYPE_CONVERTER);
        }
    };

    /**
     * Used in DiscoveryService to map subtype of a presentation message to thing type
     */
    public final static Map<Integer, ThingTypeUID> THING_UID_MAP = new HashMap<Integer, ThingTypeUID>() {

        /**
         *
         */
        private static final long serialVersionUID = -2042537863671385026L;
        {
            put(MYSENSORS_SUBTYPE_S_HUM, THING_TYPE_HUMIDITY);
            put(MYSENSORS_SUBTYPE_S_TEMP, THING_TYPE_TEMPERATURE);
            put(MYSENSORS_SUBTYPE_S_MULTIMETER, THING_TYPE_MULTIMETER);
            put(MYSENSORS_SUBTYPE_S_LIGHT, THING_TYPE_LIGHT);
            put(MYSENSORS_SUBTYPE_S_POWER, THING_TYPE_POWER);
            put(MYSENSORS_SUBTYPE_S_BARO, THING_TYPE_BARO);
            put(MYSENSORS_SUBTYPE_S_DOOR, THING_TYPE_DOOR);
            put(MYSENSORS_SUBTYPE_S_MOTION, THING_TYPE_MOTION);
            put(MYSENSORS_SUBTYPE_S_SMOKE, THING_TYPE_SMOKE);
            put(MYSENSORS_SUBTYPE_S_DIMMER, THING_TYPE_DIMMER);
            put(MYSENSORS_SUBTYPE_S_COVER, THING_TYPE_COVER);
            put(MYSENSORS_SUBTYPE_S_WIND, THING_TYPE_WIND);
            put(MYSENSORS_SUBTYPE_S_RAIN, THING_TYPE_RAIN);
            put(MYSENSORS_SUBTYPE_S_UV, THING_TYPE_UV);
            put(MYSENSORS_SUBTYPE_S_WEIGHT, THING_TYPE_WEIGHT);
            put(MYSENSORS_SUBTYPE_S_DISTANCE, THING_TYPE_DISTANCE);
            put(MYSENSORS_SUBTYPE_S_LIGHT_LEVEL, THING_TYPE_LIGHT_LEVEL);
            put(MYSENSORS_SUBTYPE_S_WATER, THING_TYPE_WATER);
            put(MYSENSORS_SUBTYPE_S_CUSTOM, THING_TYPE_CUSTOM);
            put(MYSENSORS_SUBTYPE_S_HVAC, THING_TYPE_HVAC);
            put(MYSENSORS_SUBTYPE_S_LOCK, THING_TYPE_LOCK);
            put(MYSENSORS_SUBTYPE_S_SOUND, THING_TYPE_SOUND);
            put(MYSENSORS_SUBTYPE_S_RGB_LIGHT, THING_TYPE_RGB_LIGHT);
            put(MYSENSORS_SUBTYPE_S_RGBW_LIGHT, THING_TYPE_RGBW_LIGHT);
            put(MYSENSORS_SUBTYPE_S_WATER_QUALITY, THING_TYPE_WATER_QUALITY);
            put(MYSENSORS_SUBTYPE_S_INFO, THING_TYPE_TEXT);
        }

    };

    /**
     * Inverse of the THING_UID_MAP, helps on building child for every thing type
     */
    public final static Map<ThingTypeUID, Integer> INVERSE_THING_UID_MAP = MySensorsUtility.invertMap(THING_UID_MAP,
            true);

    /** Supported Things without bridge */
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_HUMIDITY,
            THING_TYPE_TEMPERATURE, THING_TYPE_LIGHT, THING_TYPE_MULTIMETER, THING_TYPE_POWER, THING_TYPE_BARO,
            THING_TYPE_DOOR, THING_TYPE_MOTION, THING_TYPE_SMOKE, THING_TYPE_DIMMER, THING_TYPE_COVER, THING_TYPE_WIND,
            THING_TYPE_RAIN, THING_TYPE_UV, THING_TYPE_WEIGHT, THING_TYPE_DISTANCE, THING_TYPE_LIGHT_LEVEL,
            THING_TYPE_HVAC, THING_TYPE_WATER, THING_TYPE_CUSTOM, THING_TYPE_LOCK, THING_TYPE_SOUND,
            THING_TYPE_RGB_LIGHT, THING_TYPE_RGBW_LIGHT, THING_TYPE_WATER_QUALITY, THING_TYPE_MYSENSORS_MESSAGE,
            THING_TYPE_TEXT, THING_TYPE_IR_SEND, THING_TYPE_IR_RECEIVE, THING_TYPE_AIR_QUALITY);
    /** Supported bridges */
    public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_BRIDGE_SER,
            THING_TYPE_BRIDGE_ETH);

    /** Supported devices (things + brdiges) */
    public final static Collection<ThingTypeUID> SUPPORTED_DEVICE_TYPES_UIDS = Lists.newArrayList(THING_TYPE_HUMIDITY,
            THING_TYPE_TEMPERATURE, THING_TYPE_LIGHT, THING_TYPE_MULTIMETER, THING_TYPE_POWER, THING_TYPE_BARO,
            THING_TYPE_DOOR, THING_TYPE_MOTION, THING_TYPE_SMOKE, THING_TYPE_DIMMER, THING_TYPE_COVER, THING_TYPE_WIND,
            THING_TYPE_RAIN, THING_TYPE_UV, THING_TYPE_WEIGHT, THING_TYPE_DISTANCE, THING_TYPE_LIGHT_LEVEL,
            THING_TYPE_HVAC, THING_TYPE_WATER, THING_TYPE_CUSTOM, THING_TYPE_LOCK, THING_TYPE_SOUND,
            THING_TYPE_RGB_LIGHT, THING_TYPE_RGBW_LIGHT, THING_TYPE_WATER_QUALITY, THING_TYPE_MYSENSORS_MESSAGE,
            THING_TYPE_TEXT, THING_TYPE_IR_SEND, THING_TYPE_IR_RECEIVE, THING_TYPE_AIR_QUALITY, THING_TYPE_BRIDGE_SER,
            THING_TYPE_BRIDGE_ETH);
}
