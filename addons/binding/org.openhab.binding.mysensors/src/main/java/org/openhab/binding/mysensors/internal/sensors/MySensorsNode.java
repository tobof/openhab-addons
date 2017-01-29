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

import org.openhab.binding.mysensors.internal.exception.MergeException;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;

/**
 * Characteristics of a thing/node are stored here:
 * - List of children
 * - Last update (DateTime) from the node
 * - is the child reachable?
 * - battery percent (if available)
 *
 * @author Andrea Cioni
 *
 */
public class MySensorsNode {

    // Reserved ids
    public static final int MYSENSORS_NODE_ID_RESERVED_0 = 0;
    public static final int MYSENSORS_NODE_ID_RESERVED_255 = 255;

    private final int nodeId;

    private boolean reachable = true;

    private Map<Integer, MySensorsChild> chidldMap = null;

    private Date lastUpdate = null;

    private int batteryPercent = 0;

    public MySensorsNode(int nodeId) {
        if (!isValidNodeId(nodeId)) {
            throw new IllegalArgumentException("Invalid node id supplied: " + nodeId);
        }
        this.nodeId = nodeId;
        this.chidldMap = new HashMap<Integer, MySensorsChild>();
        this.lastUpdate = new Date(0);
    }

    public Map<Integer, MySensorsChild> getChildMap() {
        return chidldMap;
    }

    /**
     * Get node ID
     *
     * @return the ID of this node
     */
    public int getNodeId() {
        return nodeId;
    }

    /**
     * Add a child not null child to child to this node
     *
     * @param child to add
     */
    public void addChild(MySensorsChild child) {
        if (child == null) {
            throw new IllegalArgumentException("Null child could't be add");
        }

        synchronized (chidldMap) {
            chidldMap.put(child.getChildId(), child);
        }
    }

    /**
     * Get a child from a node
     *
     * @param childId the id of the child to get from this node
     * @return
     */
    public MySensorsChild getChild(int childId) {
        return chidldMap.get(childId);
    }

    /**
     * Set node reachable status.
     *
     * @param reachable (true=yes,false=no)
     */
    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    /**
     * Check if this node is reachable
     *
     * @return true if this node is reachable
     */
    public boolean isReachable() {
        return reachable;
    }

    /**
     * Get battery percent of this node
     *
     * @return the battery percent
     */
    public int getBatteryPercent() {
        return batteryPercent;
    }

    /**
     * Set battery percent
     *
     * @param batteryPercent that will be set
     */
    public void setBatteryPercent(int batteryPercent) {
        this.batteryPercent = batteryPercent;
    }

    /**
     * Get last update
     *
     * @return the last update, 1970-01-01 00:00 means no update received
     */
    public Date getLastUpdate() {
        synchronized (this.lastUpdate) {
            return lastUpdate;
        }
    }

    /**
     * Set last update
     *
     * @param lastUpdate
     */
    public void setLastUpdate(Date lastUpdate) {
        synchronized (this.lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }

    /**
     * Merge to child map into one.
     *
     * @param node
     *
     * @throws MergeException if try to merge to node with same child/children
     */
    public void mergeNodeChildren(MySensorsNode node) throws MergeException {

        if (node == null) {
            throw new IllegalArgumentException("Null node can't be merged");
        }

        synchronized (chidldMap) {
            try {
                mergeMap(chidldMap, node.chidldMap, false);
            } catch (Exception e) {
                throw new MergeException("Merging child map of node " + this.nodeId + " and " + node.nodeId
                        + " failed. Cause" + e.getMessage() + " (" + e.getClass().getSimpleName() + ")");
            }

        }
    }

    /**
     * Generate message from a state. This method doesn't update variable itself.
     *
     * @param childId
     * @param type
     * @param state
     *
     * @return a message ready to be sent
     */
    public MySensorsMessage updateVariableState(int childId, int type, String state) {
        MySensorsMessage msg = null;
        // TODO
        return msg;
    }

    /**
     * Check if an integer is a valid node ID
     *
     * @param ID to test
     *
     * @return true if ID is valid
     */
    public static boolean isValidNodeId(int id) {
        return (id > MYSENSORS_NODE_ID_RESERVED_0 && id < MYSENSORS_NODE_ID_RESERVED_255);
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
        result = prime * result + nodeId;
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
        if (nodeId != other.nodeId) {
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
