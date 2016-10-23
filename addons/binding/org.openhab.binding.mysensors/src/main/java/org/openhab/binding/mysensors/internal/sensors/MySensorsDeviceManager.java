package org.openhab.binding.mysensors.internal.sensors;

import java.util.Map;
import java.util.Set;

import org.openhab.binding.mysensors.internal.event.MySensorsEventType;
import org.openhab.binding.mysensors.internal.event.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.internal.event.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.exception.NoMoreIdsException;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySensorsDeviceManager implements MySensorsUpdateListener {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, MySensorsNode> nodeMap = null;

    private static MySensorsDeviceManager singleton = null;

    private MySensorsDeviceManager() {
    }

    public synchronized static MySensorsDeviceManager getDeviceManager() {
        if (singleton == null) {
            singleton = new MySensorsDeviceManager();
        }

        return singleton;
    }

    public MySensorsNode getNode(int nodeId) {
        return nodeMap.get(nodeId);
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
        return nodeMap.keySet();
    }

    public Integer reserveId() throws NoMoreIdsException {
        int newId = 1;

        Set<Integer> takenIds = getGivenIds();

        synchronized (takenIds) {
            while (newId < 255) {
                if (!takenIds.contains(newId)) {
                    nodeMap.put(newId, new MySensorsNode(newId));
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

    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {
        switch (event.getEventType()) {
            case INCOMING_MESSAGE:
                handleIncomingMessageEvent((MySensorsMessage) event.getData());
                break;
            default:
                break;
        }

    }

    private void handleIncomingMessageEvent(MySensorsMessage msg) {
        // Are we getting a Request ID Message?
        if (msg.isIdRequestMessage()) {
            answerIDRequest();
            return;
        }

        // Register node if not present
        checkNodeFound(msg);
    }

    private void checkNodeFound(MySensorsMessage msg) {
        MySensorsNode node = null;
        synchronized (nodeMap) {
            if (MySensorsNode.isValidNodeId(msg.getNodeId()) && !nodeMap.containsKey(msg.nodeId)) {
                logger.debug("Node {} found!", msg.getNodeId());
                node = new MySensorsNode(msg.nodeId);
                addNode(node);
            }
        }

        if (node != null) {
            MySensorsStatusUpdateEvent evt = new MySensorsStatusUpdateEvent(MySensorsEventType.NEW_NODE_DISCOVERED,
                    node);
            myCon.broadCastEvent(evt);
        }
    }

    /**
     * If an ID -Request from a sensor is received the controller will send an id to the sensor
     */
    private void answerIDRequest() {
        logger.info("ID Request received");

        int newId = 0;
        try {
            newId = reserveId();
            MySensorsMessage newMsg = new MySensorsMessage(255, 255, 3, 0, false, 4, newId + "");
            myCon.addMySensorsOutboundMessage(newMsg);
            logger.info("New Node in the MySensors network has requested an ID. ID is: {}", newId);
        } catch (NoMoreIdsException e) {
            logger.error("No more IDs available for this node, try cleaning cache");
        }
    }
}
