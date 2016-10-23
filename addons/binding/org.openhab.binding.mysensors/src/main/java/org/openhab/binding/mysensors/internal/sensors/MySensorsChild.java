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

public class MySensorsChild {

    private Integer childId = 0;
    private Map<Integer, MySensorsVariable> variableMap = null;

    private Date childLastUpdate = null;

    public MySensorsChild(int childId) {
        this.childId = childId;
        variableMap = new HashMap<Integer, MySensorsVariable>();

    }

    public MySensorsChild(int childId, Map<Integer, MySensorsVariable> variableMap) throws NullPointerException {
        this.childId = childId;

        if (variableMap == null) {
            throw new NullPointerException("Passed varialble map in costructor is null");
        }

        this.variableMap = variableMap;
    }

    public MySensorsVariable getVariable(int variableNum) {
        return variableMap.get(variableNum);
    }

    public int getChildId() {
        return childId;
    }

    public Date getLastUpdate() {
        return childLastUpdate;
    }

    public static boolean isValidChildId(int id) {
        return (id > 0 && id < 255);
    }

    @Override
    public String toString() {
        return "MySensorsChild [childId=" + childId + ", nodeValue=" + variableMap + "]";
    }

}
