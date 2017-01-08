/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.sensors;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.CHANNEL_MAP;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.mysensors.internal.Pair;
import org.openhab.binding.mysensors.internal.event.MySensorsBridgeConnectionEventListener;
import org.openhab.binding.mysensors.internal.event.MySensorsDeviceEventListener;
import org.openhab.binding.mysensors.internal.event.MySensorsEventObservable;
import org.openhab.binding.mysensors.internal.event.MySensorsEventRegister;
import org.openhab.binding.mysensors.internal.exception.NoMoreIdsException;
import org.openhab.binding.mysensors.internal.protocol.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ID handling for the MySensors network: Requests for IDs get answered and IDs get stored in a local cache.
 *
 * @author Andrea Cioni
 *
 */
public class MySensorsDeviceManager
        implements MySensorsEventObservable<MySensorsDeviceEventListener>, MySensorsBridgeConnectionEventListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, MySensorsNode> nodeMap = null;

    private MySensorsEventRegister<MySensorsDeviceEventListener> eventRegister;

    public MySensorsDeviceManager() {
        nodeMap = new HashMap<>();
        eventRegister = new MySensorsEventRegister<>();
    }

    public MySensorsDeviceManager(Map<Integer, MySensorsNode> nodeMap) {
        this.nodeMap = nodeMap;
        eventRegister = new MySensorsEventRegister<>();
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

        notifyNodeIdReserved(newId);

        return newId;
    }

    @Override
    public void addEventListener(MySensorsDeviceEventListener listener) {
        eventRegister.addEventListener(listener);

    }

    @Override
    public void clearAllListeners() {
        eventRegister.clearAllListeners();

    }

    @Override
    public List<MySensorsDeviceEventListener> getEventListeners() {
        return eventRegister.getEventListeners();
    }

    @Override
    public boolean isEventListenerRegisterd(MySensorsDeviceEventListener listener) {
        return eventRegister.isEventListenerRegisterd(listener);
    }

    @Override
    public void removeEventListener(MySensorsDeviceEventListener listener) {
        eventRegister.removeEventListener(listener);
    }

    @Override
    public void messageReceived(MySensorsMessage message) throws Throwable {
        handleIncomingMessage(message);
    }

    @Override
    public void bridgeStatusUpdate(MySensorsBridgeConnection connection, boolean connected) throws Throwable {
        handleBridgeStatusUpdate(connected);
    }

    private void notifyNewNodeDiscovered(MySensorsNode node) {
        synchronized (eventRegister.getEventListeners()) {
            for (MySensorsDeviceEventListener listener : eventRegister.getEventListeners()) {
                logger.trace("Broadcasting event {} to: {}", node, listener);

                try {
                    listener.newNodeDiscovered(node);
                } catch (Throwable e) {
                    logger.error("Event broadcasting throw an exception", e);
                }
            }
        }
    }

    private void notifyNodeIdReserved(Integer reserved) {
        synchronized (eventRegister.getEventListeners()) {
            for (MySensorsDeviceEventListener listener : eventRegister.getEventListeners()) {
                logger.trace("Broadcasting event {} to: {}", reserved, listener);

                try {
                    listener.nodeIdReservationDone(reserved);
                } catch (Throwable e) {
                    logger.error("Event broadcasting throw an exception", e);
                }
            }
        }
    }

    private void notifyNodeUpdateEvent(MySensorsNode node, MySensorsChild child, MySensorsVariable variable) {
        synchronized (eventRegister.getEventListeners()) {
            for (MySensorsDeviceEventListener listener : eventRegister.getEventListeners()) {
                logger.trace("Broadcasting event {} to: {}", variable, listener);

                try {
                    listener.nodeUpdateEvent(node, child, variable);
                } catch (Throwable e) {
                    logger.error("Event broadcasting throw an exception", e);
                }
            }
        }
    }

    private void notifyNodeReachEvent(MySensorsNode node, boolean reach) {
        synchronized (eventRegister.getEventListeners()) {
            for (MySensorsDeviceEventListener listener : eventRegister.getEventListeners()) {
                logger.trace("Broadcasting event {} to: {}", node, listener);

                try {
                    listener.nodeReachStatusChanged(node, reach);
                } catch (Throwable e) {
                    logger.error("Event broadcasting throw an exception", e);
                }
            }
        }
    }

    private void handleBridgeStatusUpdate(boolean connected) {
        synchronized (nodeMap) {
            for (Integer i : nodeMap.keySet()) {
                MySensorsNode node = nodeMap.get(i);
                node.setReachable(connected);
                notifyNodeReachEvent(node, connected);
            }
        }
    }

    /**
     * Handle the incoming message from serial
     *
     * @param msg the incoming message
     * @return true if ,and only if, the message is propagated to one of the defined node or message arrives from a
     *         device new device in the network
     * @throws Throwable
     */
    private boolean handleIncomingMessage(MySensorsMessage msg) throws Throwable {
        boolean ret = false;
        if (MySensorsNode.isValidNodeId(msg.nodeId) && MySensorsChild.isValidChildId(msg.childId)
                && msg.isSetReqMessage()) {
            MySensorsNode node = getNode(msg.nodeId);
            if (node != null) {
                logger.debug("Node {} found in device manager", msg.nodeId);

                node.setLastUpdate(new Date());

                MySensorsChild child = node.getChild(msg.childId);
                if (child != null) {
                    logger.debug("Child {} found in node {}", msg.childId, msg.nodeId);

                    child.setLastUpdate(new Date());

                    MySensorsVariable variable = child.getVariable(msg.msgType, msg.subType);
                    if (variable != null) {
                        variable.setValue(msg);
                        notifyNodeUpdateEvent(node, child, variable);
                        ret = true;
                    } else {
                        logger.warn("Variable {}({}) not present", msg.subType,
                                CHANNEL_MAP.get(new Pair<Integer>(msg.msgType, msg.subType)));
                    }
                } else {
                    logger.debug("Child {} not present into node {}", msg.childId, msg.nodeId);
                }
            } else {
                logger.debug("Node {} not present, send new node discovered event", msg.nodeId);

                node = new MySensorsNode(msg.nodeId);
                addNode(node);
                notifyNewNodeDiscovered(node);
                ret = true;
            }
        }

        return ret;
    }
}
