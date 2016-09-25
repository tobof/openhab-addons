package org.openhab.binding.mysensors.internal.sensors;

import java.util.HashMap;
import java.util.Set;

import org.openhab.binding.mysensors.internal.MySensorsUtility;
import org.openhab.binding.mysensors.internal.event.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.internal.event.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.exception.NoMoreIdsException;
import org.openhab.binding.mysensors.internal.protocol.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySensorsDeviceManager implements MySensorsUpdateListener {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private MySensorsBridgeConnection myCon = null;

    private HashMap<Integer, MySensorsNode> nodeMap = null;

    public MySensorsDeviceManager(MySensorsBridgeConnection myCon) {
        this.myCon = myCon;
        this.nodeMap = new HashMap<Integer, MySensorsNode>();
    }

    public MySensorsNode getNode(int nodeId) {
        return nodeMap.get(nodeId);
    }

    public void addNode(MySensorsNode node) {
        synchronized (nodeMap) {
            nodeMap.put(node.getNodeId(), node);
        }
    }

    public Set<Integer> getGivenIds() {
        return nodeMap.keySet();
    }

    public Integer reserveId() throws NoMoreIdsException {
        int newId = 1;

        clearNullOnMap();

        Set<Integer> takenIds = getGivenIds();

        synchronized (takenIds) {
            while (newId < 255) {
                if (!takenIds.contains(newId)) {
                    nodeMap.put(newId, null);
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
        if (MySensorsUtility.isIdRequestMessage(msg)) {
            answerIDRequest();
            return;
        }

        // Are we getting a Presentation Message
        if (MySensorsUtility.isPresentationMessage(msg)) {
            newChildPresented(msg);
            return;
        }
    }

    private void newChildPresented(MySensorsMessage msg) {
        if (nodeMap.containsKey(msg.nodeId)) {

        } else {

        }
    }

    /**
     * Removes null element from map, null element represent reserved, but not used, id for nodes.
     */
    private void clearNullOnMap() {
        synchronized (nodeMap) {
            for (Integer i : getGivenIds()) {
                if (getNode(i) == null) {
                    nodeMap.remove(i);
                }
            }
        }
    }

    /**
     * If an ID -Request from a sensor is received the controller will send an id to the sensor
     */
    private void answerIDRequest() {
        logger.debug("ID Request received");

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
