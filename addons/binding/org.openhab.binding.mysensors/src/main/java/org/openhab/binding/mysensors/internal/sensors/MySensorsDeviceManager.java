package org.openhab.binding.mysensors.internal.sensors;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.mysensors.internal.Pair;
import org.openhab.binding.mysensors.internal.exception.NoMoreIdsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySensorsDeviceManager {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, MySensorsNode> nodeMap = null;

    public static MySensorsDeviceManager instance = null;

    private MySensorsDeviceManager() {
        nodeMap = new HashMap<>();
    }

    public static MySensorsDeviceManager getInstance() {
        if (instance == null) {
            instance = new MySensorsDeviceManager();
        }
        return instance;
    }

    public MySensorsNode getNode(int nodeId) {
        synchronized (nodeMap) {
            return nodeMap.get(nodeId);
        }
    }

    public MySensorsChild getChild(int nodeId, int childId) {
        MySensorsChild ret = null;
        MySensorsNode node = getNode(nodeId);
        if (node != null) {
            ret = node.getChild(childId);
        }

        return ret;
    }

    public MySensorsVariable getVariable(int nodeId, int childId, Pair<Integer> typeSubType) {
        return getVariable(nodeId, childId, typeSubType.getFirst(), typeSubType.getSecond());
    }

    public MySensorsVariable getVariable(int nodeId, int childId, int messageType, int varNumber) {
        MySensorsVariable ret = null;
        MySensorsChild child = getChild(nodeId, childId);
        if (child != null) {
            ret = child.getVariable(messageType, varNumber);
        }

        return ret;
    }

    public void addNode(MySensorsNode node) {
        synchronized (nodeMap) {
            nodeMap.put(node.getNodeId(), node);
        }
    }

    public void addChild(int nodeId, MySensorsChild child) {
        synchronized (nodeMap) {
            MySensorsNode node = nodeMap.get(nodeId);
            if (node != null) {
                node.addChild(child);
            } else {
                logger.warn("Node {} not found in map", nodeId);
            }
        }
    }

    public Set<Integer> getGivenIds() {
        synchronized (nodeMap) {
            return nodeMap.keySet();
        }
    }

    public Integer reserveId() throws NoMoreIdsException {
        int newId = 1;

        synchronized (nodeMap) {
            Set<Integer> takenIds = getGivenIds();
            while (newId < 255) {
                if (!takenIds.contains(newId)) {
                    addNode(new MySensorsNode(newId));
                    break;
                } else {
                    newId++;
                }
            }
        }

        if (newId == 255) {
            throw new NoMoreIdsException();
        }

        return newId;
    }

    public void mergeNodeChilds(MySensorsNode node) {
        if (node != null) {
            MySensorsNode existingNode = getNode(node.getNodeId());
            if (existingNode != null) {
                existingNode.mergeChilds(node);
            } else {
                addNode(node);
            }
        }

    }
}
