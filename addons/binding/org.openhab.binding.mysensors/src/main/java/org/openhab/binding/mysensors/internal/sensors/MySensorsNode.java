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

public class MySensorsNode {

    private Integer nodeId = null;

    private boolean reachable = true;

    private Map<Integer, MySensorsChild<?>> chidldMap = null;

    private Date lastUpdate = null;

    public MySensorsNode(int nodeId) {
        this.nodeId = nodeId;
        this.chidldMap = new HashMap<Integer, MySensorsChild<?>>();
    }

    public int getNodeId() {
        return nodeId;
    }

    public void addChild(MySensorsChild<?> child) {
        chidldMap.put(child.getChildId(), child);
    }

    public boolean isReachable() {
        return reachable;
    }

    @Override
    public String toString() {
        return "MySensorsNode [nodeId=" + nodeId + ", chidldList=" + chidldMap + "]";
    }

}
