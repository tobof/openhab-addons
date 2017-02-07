/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.sensors;

import static org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage.*;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openhab.binding.mysensors.internal.Mergeable;
import org.openhab.binding.mysensors.internal.exception.MergeException;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_AIR_QUALITY;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_ARDUINO_NODE;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_ARDUINO_REPEATER_NODE;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_BARO;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_BINARY;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_COLOR_SENSOR;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_COVER;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_CUSTOM;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_DIMMER;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_DISTANCE;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_DOOR;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_DUST;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_GAS;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_GPS;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_HEATER;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_HUM;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_HVAC;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_INFO;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_IR;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_LIGHT_LEVEL;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_LOCK;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_MOISTURE;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_MOTION;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_MULTIMETER;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_POWER;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_RAIN;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_RGBW_LIGHT;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_RGB_LIGHT;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_SCENE_CONTROLLER;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_SMOKE;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_SOUND;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_SPRINKLER;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_TEMP;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_UV;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_VIBRATION;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_WATER;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_WATER_LEAK;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_WATER_QUALITY;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_WEIGHT;
import org.openhab.binding.mysensors.internal.sensors.child.MySensorsChild_S_WIND;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_VAR1;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_VAR2;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_VAR3;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_VAR4;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_VAR5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Every node may have one ore more children in the MySensors context.
 * Instance of this class could be obtained easy if presentation code is know (use static method fromPresentation).
 *
 * @author Andrea Cioni
 *
 */
public abstract class MySensorsChild implements Mergeable {

    // Reserved ids
    public static final int MYSENSORS_CHILD_ID_RESERVED_0 = 0;
    public static final int MYSENSORS_CHILD_ID_RESERVED_255 = 255;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Used to build child from presentation code
     */
    public final static Map<Integer, Class<? extends MySensorsChild>> PRESENTATION_TO_CHILD_CLASS = new HashMap<Integer, Class<? extends MySensorsChild>>() {

        /**
         *
         */
        private static final long serialVersionUID = -3479184996747993491L;

        {
            put(MYSENSORS_SUBTYPE_S_DOOR, MySensorsChild_S_DOOR.class);
            put(MYSENSORS_SUBTYPE_S_MOTION, MySensorsChild_S_MOTION.class);
            put(MYSENSORS_SUBTYPE_S_SMOKE, MySensorsChild_S_SMOKE.class);
            put(MYSENSORS_SUBTYPE_S_LIGHT, MySensorsChild_S_BINARY.class); // BINARY=LIGHT
            put(MYSENSORS_SUBTYPE_S_BINARY, MySensorsChild_S_BINARY.class);
            put(MYSENSORS_SUBTYPE_S_DIMMER, MySensorsChild_S_DIMMER.class);
            put(MYSENSORS_SUBTYPE_S_COVER, MySensorsChild_S_COVER.class);
            put(MYSENSORS_SUBTYPE_S_TEMP, MySensorsChild_S_TEMP.class);
            put(MYSENSORS_SUBTYPE_S_HUM, MySensorsChild_S_HUM.class);
            put(MYSENSORS_SUBTYPE_S_BARO, MySensorsChild_S_BARO.class);
            put(MYSENSORS_SUBTYPE_S_WIND, MySensorsChild_S_WIND.class);
            put(MYSENSORS_SUBTYPE_S_RAIN, MySensorsChild_S_RAIN.class);
            put(MYSENSORS_SUBTYPE_S_UV, MySensorsChild_S_UV.class);
            put(MYSENSORS_SUBTYPE_S_WEIGHT, MySensorsChild_S_WEIGHT.class);
            put(MYSENSORS_SUBTYPE_S_POWER, MySensorsChild_S_POWER.class);
            put(MYSENSORS_SUBTYPE_S_HEATER, MySensorsChild_S_HEATER.class);
            put(MYSENSORS_SUBTYPE_S_DISTANCE, MySensorsChild_S_DISTANCE.class);
            put(MYSENSORS_SUBTYPE_S_LIGHT_LEVEL, MySensorsChild_S_LIGHT_LEVEL.class);
            put(MYSENSORS_SUBTYPE_S_LOCK, MySensorsChild_S_LOCK.class);
            put(MYSENSORS_SUBTYPE_S_IR, MySensorsChild_S_IR.class);
            put(MYSENSORS_SUBTYPE_S_WATER, MySensorsChild_S_WATER.class);
            put(MYSENSORS_SUBTYPE_S_AIR_QUALITY, MySensorsChild_S_AIR_QUALITY.class);
            put(MYSENSORS_SUBTYPE_S_CUSTOM, MySensorsChild_S_CUSTOM.class);
            put(MYSENSORS_SUBTYPE_S_RGB_LIGHT, MySensorsChild_S_RGB_LIGHT.class);
            put(MYSENSORS_SUBTYPE_S_RGBW_LIGHT, MySensorsChild_S_RGBW_LIGHT.class);
            put(MYSENSORS_SUBTYPE_S_HVAC, MySensorsChild_S_HVAC.class);
            put(MYSENSORS_SUBTYPE_S_MULTIMETER, MySensorsChild_S_MULTIMETER.class);
            put(MYSENSORS_SUBTYPE_S_SPRINKLER, MySensorsChild_S_SPRINKLER.class);
            put(MYSENSORS_SUBTYPE_S_WATER_LEAK, MySensorsChild_S_WATER_LEAK.class);
            put(MYSENSORS_SUBTYPE_S_SOUND, MySensorsChild_S_SOUND.class);
            put(MYSENSORS_SUBTYPE_S_VIBRATION, MySensorsChild_S_VIBRATION.class);
            put(MYSENSORS_SUBTYPE_S_MOISTURE, MySensorsChild_S_MOISTURE.class);
            put(MYSENSORS_SUBTYPE_S_INFO, MySensorsChild_S_INFO.class);
            put(MYSENSORS_SUBTYPE_S_GAS, MySensorsChild_S_GAS.class);
            put(MYSENSORS_SUBTYPE_S_GPS, MySensorsChild_S_GPS.class);
            put(MYSENSORS_SUBTYPE_S_WATER_QUALITY, MySensorsChild_S_WATER_QUALITY.class);
            put(MYSENSORS_SUBTYPE_S_SCENE_CONTROLLER, MySensorsChild_S_SCENE_CONTROLLER.class);
            put(MYSENSORS_SUBTYPE_S_DUST, MySensorsChild_S_DUST.class);
            put(MYSENSORS_SUBTYPE_S_COLOR_SENSOR, MySensorsChild_S_COLOR_SENSOR.class);
            put(MYSENSORS_SUBTYPE_S_ARDUINO_REPEATER_NODE, MySensorsChild_S_ARDUINO_REPEATER_NODE.class);
            put(MYSENSORS_SUBTYPE_S_ARDUINO_NODE, MySensorsChild_S_ARDUINO_NODE.class);
        }

    };

    private final int childId;

    private Optional<MySensorsChildConfig> childConfig;

    private Map<Integer, MySensorsVariable> variableMap = null;

    private Date lastUpdate = null;

    private int presentationCode;

    public MySensorsChild(int childId) {
        if (!isValidChildId(childId)) {
            throw new IllegalArgumentException("Invalid child id supplied: " + childId);
        }
        this.childId = childId;
        variableMap = new HashMap<Integer, MySensorsVariable>();
        lastUpdate = new Date(0);
        childConfig = Optional.empty();
        addCommonVariables();
    }

    public MySensorsChild(int childId, MySensorsChildConfig config) {
        if (!isValidChildId(childId)) {
            throw new IllegalArgumentException("Invalid child id supplied: " + childId);
        }

        if (config == null) {
            throw new IllegalArgumentException("Invalid config supplied for child: " + childId);
        }

        this.childId = childId;
        variableMap = new HashMap<Integer, MySensorsVariable>();
        lastUpdate = new Date(0);
        childConfig = Optional.of(config);
        addCommonVariables();
    }

    /**
     * Add a variable to a child
     *
     * @param var the non-null variable to add to this child
     *
     * @throws NullPointerException if var is null
     */
    public void addVariable(MySensorsVariable var) throws NullPointerException {

        if (var == null) {
            throw new NullPointerException("Cannot add a null variable");
        }

        synchronized (variableMap) {
            if (variableMap.containsKey(var.getType())) {
                logger.warn("Overwrite variable: " + var.getType());
            }

            variableMap.put(var.getType(), var);

        }
    }

    /**
     * Get MySensorsVariable of this child
     *
     * @param type the integer of the subtype
     * @return one MySensorsVariable if present, otherwise null
     */
    public MySensorsVariable getVariable(int type) {
        synchronized (variableMap) {
            return variableMap.get(type);
        }
    }

    /**
     * Get child id
     *
     * @return child id
     */
    public int getChildId() {
        return childId;
    }

    /**
     * Get child last update
     *
     * @return the date represent when the child has received and update from network. Default value is 1970/01/01-00:00
     */
    public Date getLastUpdate() {
        synchronized (lastUpdate) {
            return lastUpdate;
        }
    }

    /**
     * Set child last update
     *
     * @param childLastUpdate new date represents when child has received an update from network
     */
    public void setLastUpdate(Date childLastUpdate) {
        synchronized (this.lastUpdate) {
            this.lastUpdate = childLastUpdate;
        }
    }

    public int getPresentationCode() {
        return presentationCode;
    }

    public void setPresentationCode(int presentationCode) {
        this.presentationCode = presentationCode;
    }

    public Optional<MySensorsChildConfig> getChildConfig() {
        return childConfig;
    }

    public void setChildConfig(MySensorsChildConfig childConfig) {
        this.childConfig = Optional.of(childConfig);
    }

    @Override
    public void merge(Object o) throws MergeException {
        if (o == null || !(o instanceof MySensorsChild)) {
            throw new MergeException("Invalid object to merge");
        }

        MySensorsChild child = (MySensorsChild) o;

        // Merge configurations
        if (child.childConfig.isPresent() && !childConfig.isPresent()) {
            childConfig = child.childConfig;
        } else if (child.childConfig.isPresent() && childConfig.isPresent()) {
            childConfig.get().merge(child.childConfig);
        }

    }

    private void addCommonVariables() {
        addVariable(new MySensorsVariable_V_VAR1());
        addVariable(new MySensorsVariable_V_VAR2());
        addVariable(new MySensorsVariable_V_VAR3());
        addVariable(new MySensorsVariable_V_VAR4());
        addVariable(new MySensorsVariable_V_VAR5());
    }

    /**
     * Static method to ensure if one id belongs to a valid range
     *
     * @param id, child id probably from a message
     * @return true if passed id is valid
     */
    public static boolean isValidChildId(int id) {
        return (id >= MYSENSORS_CHILD_ID_RESERVED_0 && id < MYSENSORS_CHILD_ID_RESERVED_255);
    }

    /**
     * Generate an instance of MySensorsChild from a presentation code.
     *
     * @param presentationCode presentation code in a presentation message
     * @param childId the id to set to the generated child
     *
     * @return an instance of a child
     */
    public static MySensorsChild fromPresentation(int presentationCode, int childId) {
        MySensorsChild ret;

        if (PRESENTATION_TO_CHILD_CLASS.containsKey(presentationCode)) {
            try {
                Class<? extends MySensorsChild> cls = PRESENTATION_TO_CHILD_CLASS.get(presentationCode);
                Constructor<? extends MySensorsChild> constr = cls.getConstructor(int.class);
                ret = constr.newInstance(childId);
            } catch (Exception e) {
                LoggerFactory.getLogger(MySensorsChild.class)
                        .error("Reflection has failed for presentation {}, childId:", presentationCode, childId, e);
                ret = null;
            }
        } else {
            throw new IllegalArgumentException(
                    "Presentation code (" + presentationCode + ") or child id not valid (" + childId + ")");
        }

        return ret;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + childId;
        result = prime * result + presentationCode;
        result = prime * result + ((variableMap == null) ? 0 : variableMap.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MySensorsChild other = (MySensorsChild) obj;
        if (childId != other.childId) {
            return false;
        }
        if (presentationCode != other.presentationCode) {
            return false;
        }
        if (variableMap == null) {
            if (other.variableMap != null) {
                return false;
            }
        } else if (!variableMap.equals(other.variableMap)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MySensorsChild [childId=" + childId + ", nodeValue=" + variableMap + "]";
    }

}
