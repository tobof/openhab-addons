/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.sensors;

import static org.openhab.binding.mysensors.internal.MySensorsUtility.mergeMap;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MySensorsNode {

    private Integer nodeId = null;

    private boolean reachable = true;

    private Map<Integer, MySensorsChild> chidldMap = null;

    private Date lastUpdate = null;

    private int batteryPercent = 0;

    public MySensorsNode(int nodeId) {
        this.nodeId = nodeId;
        this.chidldMap = new HashMap<Integer, MySensorsChild>();
    }

    public MySensorsNode(int nodeId, Map<Integer, MySensorsChild> chidldMap) {
        this.nodeId = nodeId;
        this.chidldMap = chidldMap;
    }

    public Map<Integer, MySensorsChild> getChildMap() {
        return chidldMap;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void addChild(MySensorsChild child) {
        synchronized (chidldMap) {
            chidldMap.put(child.getChildId(), child);
        }
    }

    public MySensorsChild getChild(int childId) {
        return chidldMap.get(childId);
    }

    public boolean isReachable() {
        return reachable;
    }

    public int getBatteryPercent() {
        return batteryPercent;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void mergeNodeChilds(MySensorsNode node) {
        synchronized (chidldMap) {
            mergeMap(chidldMap, node.chidldMap);
        }
    }

    public static boolean isValidNodeId(int id) {
        return (id > 0 && id < 255);
    }

    public static boolean isValidNode(MySensorsNode n) {
        return (n != null) && (isValidNodeId(n.nodeId));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + batteryPercent;
        result = prime * result + ((chidldMap == null) ? 0 : chidldMap.hashCode());
        result = prime * result + ((lastUpdate == null) ? 0 : lastUpdate.hashCode());
        result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
        result = prime * result + (reachable ? 1231 : 1237);
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
        MySensorsNode other = (MySensorsNode) obj;
        if (batteryPercent != other.batteryPercent) {
            return false;
        }
        if (chidldMap == null) {
            if (other.chidldMap != null) {
                return false;
            }
        } else if (!chidldMap.equals(other.chidldMap)) {
            return false;
        }
        if (lastUpdate == null) {
            if (other.lastUpdate != null) {
                return false;
            }
        } else if (!lastUpdate.equals(other.lastUpdate)) {
            return false;
        }
        if (nodeId == null) {
            if (other.nodeId != null) {
                return false;
            }
        } else if (!nodeId.equals(other.nodeId)) {
            return false;
        }
        if (reachable != other.reachable) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MySensorsNode [nodeId=" + nodeId + ", childNumber=" + chidldMap.size() + ", chidldList=" + chidldMap
                + "]";
    }

}
