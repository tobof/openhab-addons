/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.sensors;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    private Optional<MySensorsNodeConfig> nodeConfig;

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
        this.nodeConfig = Optional.empty();
        this.lastUpdate = new Date(0);
    }

    public MySensorsNode(int nodeId, MySensorsNodeConfig config) {
        if (!isValidNodeId(nodeId)) {
            throw new IllegalArgumentException("Invalid node id supplied: " + nodeId);
        }

        if (config == null) {
            throw new IllegalArgumentException("Invalid config supplied for node: " + nodeId);
        }

        this.nodeId = nodeId;
        this.chidldMap = new HashMap<Integer, MySensorsChild>();
        this.nodeConfig = Optional.of(config);
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
     * Get optional node configuration
     *
     * @return the Optional that could contains {@link MySensorsNodeConfig}
     */
    public Optional<MySensorsNodeConfig> getNodeConfig() {
        return nodeConfig;
    }

    /**
     * Set configuration for node
     *
     * @param nodeConfig is a valid instance of {@link MySensorsNodeConfig}ß
     */
    public void setNodeConfig(MySensorsNodeConfig nodeConfig) {
        this.nodeConfig = Optional.of(nodeConfig);
    }

    /**
     * Merge to node into one.
     *
     * @param node
     *
     * @throws MergeException if try to merge to node with same child/children
     */
    public void merge(Object o) throws MergeException {

        if (o == null || !(o instanceof MySensorsNode)) {
            throw new MergeException("Invalid object to merge");
        }

        MySensorsNode node = (MySensorsNode) o;

        // Merge configurations
        if (node.nodeConfig.isPresent() && !nodeConfig.isPresent()) {
            nodeConfig = node.nodeConfig;
        } else if (node.nodeConfig.isPresent() && nodeConfig.isPresent()) {
            nodeConfig.get().merge(node.nodeConfig.get());
        }

        synchronized (chidldMap) {
            for (Integer i : node.chidldMap.keySet()) {
                MySensorsChild child = node.chidldMap.get(i);
                chidldMap.merge(i, child, (child1, child2) -> {
                    child1.merge(child2);
                    return child1;
                });
            }

        }
    }

    /**
     * Generate message from a state. This method doesn't update variable itself.
     * No check will be performed on value of state parameter
     *
     * @param childId
     * @param type
     * @param state
     *
     * @return a non-null message ready to be sent if childId/type are available on this node
     *
     * @throws NullPointerException if state is null
     */
    public MySensorsMessage updateVariableState(int childId, int type, String state) {
        MySensorsMessage msg = null;

        if (state == null) {
            throw new NullPointerException("State is null");
        }

        synchronized (chidldMap) {
            MySensorsChild child = getChild(childId);
            MySensorsChildConfig childConfig = (child.getChildConfig().isPresent()) ? child.getChildConfig().get()
                    : new MySensorsChildConfig();
            if (child != null) {
                MySensorsVariable var = child.getVariable(type);
                if (var != null) {
                    msg = new MySensorsMessage();

                    // MySensors
                    msg.setNodeId(nodeId);
                    msg.setChildId(childId);
                    msg.setMsgType(MySensorsMessage.MYSENSORS_MSG_TYPE_SET);
                    msg.setSubType(type);
                    msg.setAck(childConfig.getRequestAck());
                    msg.setMsg(state);

                    // Optional
                    msg.setRevert(childConfig.getRevertState());
                    msg.setSmartSleep(childConfig.getSmartSleep());
                }
            }
        }

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
        result = prime * result + ((chidldMap == null) ? 0 : chidldMap.hashCode());
        result = prime * result + nodeId;
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
