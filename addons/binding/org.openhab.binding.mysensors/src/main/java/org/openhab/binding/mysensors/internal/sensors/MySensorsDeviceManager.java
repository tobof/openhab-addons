package org.openhab.binding.mysensors.internal.sensors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.mysensors.internal.Pair;
import org.openhab.binding.mysensors.internal.exception.NoMoreIdsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySensorsDeviceManager {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, MySensorsNode> nodeMap = null;

    public static MySensorsDeviceManager instance = null;

    public MySensorsDeviceManager() {
        nodeMap = new HashMap<>();
    }

    public MySensorsDeviceManager(Map<Integer, MySensorsNode> nodeMap) {
        this.nodeMap = nodeMap;
    }

    private static MySensorsDeviceManager getInstance() {
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
            if (nodeMap.containsKey(node.getNodeId())) {
                logger.warn("Overwriting previous node, it was lost.");
            }
            nodeMap.put(node.getNodeId(), node);
        }
    }

    public void addNode(MySensorsNode node, boolean mergeIfExist) {
        MySensorsNode exist = null;
        if (mergeIfExist && ((exist = getNode(node.getNodeId())) != null)) {
            logger.info("Merging child map: {} with: {}", exist.getChildMap(), node.getChildMap());
            exist.mergeNodeChilds(node);
            logger.trace("Merging result is: {}", exist.getChildMap());
        } else {
            logger.info("Adding device {}", node.toString());
            addNode(node);
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

    public List<Integer> getGivenIds() {
        synchronized (nodeMap) {
            return new ArrayList<Integer>(nodeMap.keySet());
        }
    }

    public Integer reserveId() throws NoMoreIdsException {
        int newId = 1;

        synchronized (nodeMap) {
            List<Integer> takenIds = getGivenIds();
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
}
