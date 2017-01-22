/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.gateway;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.mysensors.internal.MySensorsUtility;
import org.openhab.binding.mysensors.internal.event.MySensorsEventRegister;
import org.openhab.binding.mysensors.internal.event.MySensorsGatewayEventListener;
import org.openhab.binding.mysensors.internal.exception.NoMoreIdsException;
import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;
import org.openhab.binding.mysensors.internal.protocol.ip.MySensorsIpConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.protocol.serial.MySensorsSerialConnection;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main access point of all the function of MySensors Network, some of there are
 * -ID handling for the MySensors network: Requests for IDs get answered and IDs get stored in a local cache.
 * -Updating sensors variable and status information
 *
 * @author Andrea Cioni
 *
 */
public class MySensorsGateway implements MySensorsGatewayEventListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, MySensorsNode> nodeMap;

    private MySensorsEventRegister myEventRegister;

    private MySensorsAbstractConnection myCon;

    private MySensorsGatewayConfig myConf;

    private MySensorsNetworkSanityChecker myNetSanCheck;

    public MySensorsGateway() {
        nodeMap = new HashMap<>();
        this.myEventRegister = new MySensorsEventRegister();
    }

    public MySensorsGateway(Map<Integer, MySensorsNode> nodeMap) {
        this.nodeMap = nodeMap;
        this.myEventRegister = new MySensorsEventRegister();
    }

    public boolean setup(MySensorsGatewayConfig myConf) {
        boolean ret = false;

        if (myConf != null) {
            if (myCon != null) {
                throw new IllegalStateException("Connection is walredy instantiated");
            }

            this.myConf = myConf;

            switch (myConf.getGatewayType()) {
                case SERIAL:
                    myCon = new MySensorsSerialConnection(myConf, myEventRegister);
                    ret = true;
                    break;
                case IP:
                    myCon = new MySensorsIpConnection(myConf, myEventRegister);
                    ret = true;
                    break;
            }
        } else {
            logger.error("Invalid configuration supplied: {}", myConf);
        }

        return ret;
    }

    public void startup() {

        myCon.initialize();

        myEventRegister.addEventListener(this);

        if (myConf.getEnableNetworkSanCheck()) {
            myNetSanCheck = new MySensorsNetworkSanityChecker(myCon, myEventRegister);
        }
    }

    public void shutdown() {
        myEventRegister.clearAllListeners();

        if (myNetSanCheck != null) {
            myNetSanCheck.stop();
        }

        if (myCon != null) {
            myCon.destroy();
            myCon = null;
        }
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

    public MySensorsVariable getVariable(int nodeId, int childId, int type) {
        MySensorsVariable ret = null;
        MySensorsChild child = getChild(nodeId, childId);
        if (child != null) {
            ret = child.getVariable(type);
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
        synchronized (nodeMap) {
            MySensorsNode exist = null;
            if (mergeIfExist && ((exist = getNode(node.getNodeId())) != null)
                    && !MySensorsUtility.containsSameKey(node.getChildMap(), exist.getChildMap())) {
                logger.debug("Merging child map: {} with: {}", exist.getChildMap(), node.getChildMap());
                exist.mergeNodeChildren(node);
                logger.trace("Merging result is: {}", exist.getChildMap());
            } else {
                logger.debug("Adding device {}", node.toString());
                addNode(node);
            }
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
            while (newId < MySensorsNode.MYSENSORS_NODE_ID_RESERVED_255) {
                if (!takenIds.contains(newId)) {
                    addNode(new MySensorsNode(newId));
                    break;
                } else {
                    newId++;
                }
            }
        }

        if (newId == MySensorsNode.MYSENSORS_NODE_ID_RESERVED_255) {
            throw new NoMoreIdsException();
        }

        myEventRegister.notifyNodeIdReserved(newId);

        return newId;
    }

    public void addEventListener(MySensorsGatewayEventListener listener) {
        myEventRegister.addEventListener(listener);

    }

    public void removeEventListener(MySensorsGatewayEventListener listener) {
        myEventRegister.removeEventListener(listener);

    }

    public boolean isEventListenerRegisterd(MySensorsGatewayEventListener listener) {
        return myEventRegister.isEventListenerRegisterd(listener);
    }

    public void sendMessage(MySensorsMessage message) {
        try {
            handleIncomingOutgoingMessage(message);
        } catch (Throwable e) {
            logger.error("Handling outgoing message throw an exception", e);
        }

        myCon.addMySensorsOutboundMessage(message);
    }

    @Override
    public void messageReceived(MySensorsMessage message) throws Throwable {
        if (!handleIncomingOutgoingMessage(message)) {
            handleSpecialMessageEvent(message);
        }
    }

    @Override
    public void ackNotReceived(MySensorsMessage msg) throws Throwable {
        if (MySensorsNode.isValidNodeId(msg.getNodeId()) && MySensorsChild.isValidChildId(msg.getChildId())
                && msg.isSetReqMessage()) {
            MySensorsNode node = getNode(msg.getNodeId());
            if (node != null) {
                logger.debug("Node {} found in device manager", msg.getNodeId());

                MySensorsChild child = node.getChild(msg.getChildId());
                if (child != null) {
                    logger.debug("Child {} found in node {}", msg.getChildId(), msg.getNodeId());

                    MySensorsVariable variable = child.getVariable(msg.getSubType());
                    if (variable != null) {
                        if (variable.isRevertible()) {
                            logger.debug("Variable {} found, it will be reverted to last know state",
                                    variable.getClass().getSimpleName());
                            variable.revertValue();
                        } else {
                            logger.error("Could not revert variable {}, no previous value is present",
                                    variable.getClass().getSimpleName());
                        }

                    } else {
                        logger.warn("Variable {} not present", msg.getSubType());
                    }
                } else {
                    logger.debug("Child {} not present into node {}", msg.getChildId(), msg.getNodeId());
                }
            }
        }
    }

    @Override
    public void connectionStatusUpdate(MySensorsAbstractConnection connection, boolean connected) throws Throwable {
        if (myNetSanCheck != null) {
            if (connected) {
                myNetSanCheck.start();
            } else {
                myNetSanCheck.stop();
            }
        }

        handleBridgeStatusUpdate(connected);
    }

    public MySensorsGatewayConfig getConfiguration() {
        return myConf;
    }

    private void handleBridgeStatusUpdate(boolean connected) {
        synchronized (nodeMap) {
            for (Integer i : nodeMap.keySet()) {
                MySensorsNode node = nodeMap.get(i);
                node.setReachable(connected);
                myEventRegister.notifyNodeReachEvent(node, connected);
            }
        }
    }

    /**
     * Handle the incoming/outgoing message from serial
     *
     * @param msg the incoming/outgoing message
     * @return true if ,and only if:
     *         -the message is propagated to one of the defined node or
     *         -message arrives from a device new device in the network or
     *         -message is REQ type and variable is defined for it
     *
     * @throws Throwable
     */
    private boolean handleIncomingOutgoingMessage(MySensorsMessage msg) throws Throwable {
        boolean ret = false;
        if (MySensorsNode.isValidNodeId(msg.getNodeId()) && MySensorsChild.isValidChildId(msg.getChildId())
                && msg.isSetReqMessage()) {
            MySensorsNode node = getNode(msg.getNodeId());
            if (node != null) {
                logger.debug("Node {} found in device manager", msg.getNodeId());

                node.setLastUpdate(new Date());

                MySensorsChild child = node.getChild(msg.getChildId());
                if (child != null) {
                    logger.debug("Child {} found in node {}", msg.getChildId(), msg.getNodeId());

                    child.setLastUpdate(new Date());

                    MySensorsVariable variable = child.getVariable(msg.getSubType());
                    if (variable != null) {

                        if (msg.isSetMessage()) {
                            logger.trace("Variable {}({}) found in child, pre-update value: {}",
                                    variable.getClass().getSimpleName(), variable.getType(), variable.getValue());
                            variable.setValue(msg);
                            logger.trace("Variable {}({}) found in child, post-update value: {}",
                                    variable.getClass().getSimpleName(), variable.getType(), variable.getValue());
                            variable.setLastUpdate(new Date());

                            myEventRegister.notifyNodeUpdateEvent(node, child, variable, false);
                        } else {
                            String value = variable.getValue();
                            if (value != null) {
                                logger.debug("Request received!");
                                msg.setMsgType(MySensorsMessage.MYSENSORS_MSG_TYPE_SET);
                                msg.setMsg(value);

                                // Do not use sendMessage method (it set the value to the channel again), just send it
                                // over
                                // connection
                                myCon.addMySensorsOutboundMessage(msg);
                            } else {
                                logger.warn("Request received, but variable state is not yet defined");
                            }
                        }

                        ret = true;
                    } else {
                        logger.warn("Variable {} not present", msg.getSubType());
                    }
                } else {
                    logger.debug("Child {} not present into node {}", msg.getChildId(), msg.getNodeId());
                }
            } else {
                logger.debug("Node {} not present, send new node discovered event", msg.getNodeId());

                // TODO Detect if message is Presentation, if yes create not only the node but all the hierarchy
                // associated

                node = new MySensorsNode(msg.getNodeId());
                addNode(node);
                myEventRegister.notifyNewNodeDiscovered(node);
                ret = true;
            }
        }

        return ret;
    }

    private void handleSpecialMessageEvent(MySensorsMessage msg) {

        // Is this an I_CONFIG message?
        if (msg.isIConfigMessage()) {
            answerIConfigMessage(msg);
        }

        // Is this an I_TIME message?
        if (msg.isITimeMessage()) {
            answerITimeMessage(msg);
        }

        // Requesting ID
        if (msg.isIdRequestMessage()) {
            answerIDRequest();
        }

        // Is this an I_HEARTBEAT_RESPONSE
        if (msg.isHeartbeatResponseMessage()) {
            handleIncomingHeartbeatMessage(msg);
        }
    }

    /**
     * Answer to I_TIME message for gateway time request from sensor
     *
     * @param msg, the incoming I_TIME message from sensor
     */
    private void answerITimeMessage(MySensorsMessage msg) {
        logger.info("I_TIME request received from {}, answering...", msg.getNodeId());

        String time = Long.toString(System.currentTimeMillis() / 1000);
        MySensorsMessage newMsg = new MySensorsMessage(msg.getNodeId(), msg.getChildId(),
                MySensorsMessage.MYSENSORS_MSG_TYPE_INTERNAL, 0, false, MySensorsMessage.MYSENSORS_SUBTYPE_I_TIME,
                time);
        myCon.addMySensorsOutboundMessage(newMsg);

    }

    /**
     * Answer to I_CONFIG message for imperial/metric request from sensor
     *
     * @param msg, the incoming I_CONFIG message from sensor
     */
    private void answerIConfigMessage(MySensorsMessage msg) {
        boolean imperial = myConf.getImperial();
        String iConfig = imperial ? "I" : "M";

        logger.debug("I_CONFIG request received from {}, answering: (is imperial?){}", iConfig, imperial);

        MySensorsMessage newMsg = new MySensorsMessage(msg.getNodeId(), msg.getChildId(),
                MySensorsMessage.MYSENSORS_MSG_TYPE_INTERNAL, 0, false, MySensorsMessage.MYSENSORS_SUBTYPE_I_CONFIG,
                iConfig);
        myCon.addMySensorsOutboundMessage(newMsg);

    }

    /**
     * If an ID-Request from a sensor is received the controller will send an id to the sensor
     */
    private void answerIDRequest() {
        logger.info("ID Request received");

        int newId = 0;
        try {
            newId = reserveId();
            logger.info("New Node in the MySensors network has requested an ID. ID is: {}", newId);
            MySensorsMessage newMsg = new MySensorsMessage(MySensorsNode.MYSENSORS_NODE_ID_RESERVED_255,
                    MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_255, MySensorsMessage.MYSENSORS_MSG_TYPE_INTERNAL,
                    MySensorsMessage.MYSENSORS_ACK_FALSE, false, 4, newId + "");
            myCon.addMySensorsOutboundMessage(newMsg);
        } catch (NoMoreIdsException e) {
            logger.error("No more IDs available for this node, you could try cleaning cache file");
        }
    }

    /**
     * If a heartbeat is received from a node the queue should be checked
     * for pending messages for this node. If a message is pending it has to be send immediately.
     *
     * @param msg The heartbeat message received from a node.
     */
    private void handleIncomingHeartbeatMessage(MySensorsMessage msg) {
        logger.debug("I_HEARTBEAT_RESPONSE received from {}.", msg.getNodeId());
        myCon.checkPendingSmartSleepMessage(msg.getNodeId());
    }

}
