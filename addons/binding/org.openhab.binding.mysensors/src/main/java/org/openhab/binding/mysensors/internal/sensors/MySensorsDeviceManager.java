/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.sensors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.mysensors.internal.Pair;
import org.openhab.binding.mysensors.internal.exception.NoMoreIdsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ID handling for the MySensors network: Requests for IDs get answered and IDs get stored in a local cache.
 *
 * @author Andrea Cioni
 *
 */
public class MySensorsDeviceManager {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, MySensorsNode> nodeMap = null;

    public MySensorsDeviceManager() {
        nodeMap = new HashMap<>();
    }

    public MySensorsDeviceManager(Map<Integer, MySensorsNode> nodeMap) {
        this.nodeMap = nodeMap;
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

    /**
     * Simple method that add node to DeviceManager (only if node is not present previously).
     *
     * @param node the node to add
     */
    public void addNode(MySensorsNode node) {
        synchronized (nodeMap) {
            if (nodeMap.containsKey(node.getNodeId())) {
                logger.warn("Overwriting previous node, it was lost.");
            }
            nodeMap.put(node.getNodeId(), node);
        }
    }

    /**
     * Add node to device manager
     *
     * @param node the node to add
     * @param mergeIfExist if true and node is already present that two nodes will be merged in one
     */
    public void addNode(MySensorsNode node, boolean mergeIfExist) {
        MySensorsNode exist = null;
        if (mergeIfExist && ((exist = getNode(node.getNodeId())) != null)) {
            logger.debug("Merging child map: {} with: {}", exist.getChildMap(), node.getChildMap());
            exist.mergeNodeChilds(node);
            logger.trace("Merging result is: {}", exist.getChildMap());
        } else {
            logger.debug("Adding device {}", node.toString());
            addNode(node);
        }
    }

    /**
     * Add child to node
     *
     * @param nodeId the id of the node to add the child
     * @param child the child to add
     */
    public void addChild(int nodeId, MySensorsChild child) throws IllegalArgumentException {
        synchronized (nodeMap) {
            MySensorsNode node = nodeMap.get(nodeId);
            if (node != null) {
                node.addChild(child);
            } else {
                logger.warn("Node {} not found in map", nodeId);
            }
        }
    }

    /**
     * @return a Set of Ids that is already used and known to the binding.
     */
    public List<Integer> getGivenIds() {
        synchronized (nodeMap) {
            return new ArrayList<Integer>(nodeMap.keySet());
        }
    }

    /**
     * Reserve an id for network, mainly for request id messages
     *
     * @return a free id not present in node map
     * @throws NoMoreIdsException if no more ids are available to be reserved
     */
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
