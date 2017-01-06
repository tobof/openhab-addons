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

/**
 * Every thing/node may have one ore more childs in the MySensors context.
 *
 * @author Andrea Cioni
 *
 */
public class MySensorsChild {

    private Integer childId = 0;
    private Map<Pair<Integer>, MySensorsVariable> variableMap = null;

    private Date lastUpdate = null;

    public MySensorsChild(int childId) {
        this.childId = childId;
        variableMap = new HashMap<Pair<Integer>, MySensorsVariable>();
        lastUpdate = new Date(0);
    }

    public MySensorsChild(int childId, Map<Pair<Integer>, MySensorsVariable> variableMap) throws NullPointerException {
        this.childId = childId;

        if (variableMap == null) {
            throw new NullPointerException("Passed varialble map in costructor is null");
        }

        this.variableMap = variableMap;
        lastUpdate = new Date(0);
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
        return (id >= 0 && id < 255);
    }

    @Override
    public String toString() {
        return "MySensorsChild [childId=" + childId + ", nodeValue=" + variableMap + "]";
    }

}
