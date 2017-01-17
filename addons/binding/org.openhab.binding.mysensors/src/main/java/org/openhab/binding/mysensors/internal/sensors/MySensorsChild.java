/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.sensors;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.mysensors.internal.Pair;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_VAR1;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_VAR2;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_VAR3;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_VAR4;
import org.openhab.binding.mysensors.internal.sensors.variable.MySensorsVariable_V_VAR5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Every thing/node may have one ore more children in the MySensors context.
 *
 * @author Andrea Cioni
 *
 */
public class MySensorsChild {

    // Reserved ids
    public static final int MYSENSORS_CHILD_ID_RESERVED_0 = 0;
    public static final int MYSENSORS_CHILD_ID_RESERVED_255 = 255;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private Integer childId = 0;
    private Map<Integer, MySensorsVariable> variableMap = null;

    private Date lastUpdate = null;

    public MySensorsChild(int childId) {
        this.childId = childId;
        variableMap = new HashMap<Integer, MySensorsVariable>();
        lastUpdate = new Date(0);
        addCommonVariables();
    }

    public void addVariable(MySensorsVariable var) throws NullPointerException {

        if (var == null) {
            throw new NullPointerException("Cannot add a null variable");
        }

        synchronized (variableMap) {
            if (variableMap.containsKey(var.getType())) {
                logger.warn("Overwrite variable");
            }

            variableMap.put(var.getType(), var);

        }
    }

    /**
     * Get MySensorsVariable of this child
     *
     * @param messageType the integer of message type
     * @param variableNum the integer of the subtype
     * @return one MySensorsVariable if present, otherwise null
     */
    public MySensorsVariable getVariable(int messageType, int variableNum) {
        synchronized (variableMap) {
            return variableMap.get(new Pair<Integer>(messageType, variableNum));
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

    /**
     * Static method to ensure if one id belongs to a valid range
     *
     * @param id, child id probably from a message
     * @return true if passed id is valid
     */
    public static boolean isValidChildId(int id) {
        return (id >= MYSENSORS_CHILD_ID_RESERVED_0 && id < MYSENSORS_CHILD_ID_RESERVED_255);
    }

    private void addCommonVariables() {
        addVariable(new MySensorsVariable_V_VAR1());
        addVariable(new MySensorsVariable_V_VAR2());
        addVariable(new MySensorsVariable_V_VAR3());
        addVariable(new MySensorsVariable_V_VAR4());
        addVariable(new MySensorsVariable_V_VAR5());
    }

    @Override
    public String toString() {
        return "MySensorsChild [childId=" + childId + ", nodeValue=" + variableMap + "]";
    }

}
