/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociationGroup;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveAssociationEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Multi Instance Association command class.
 * This allows reading and writing of node association parameters with instance values
 *
 * @author Chris Jackson
 */
@XStreamAlias("multiAssociationCommandClass")
public class ZWaveMultiAssociationCommandClass extends ZWaveCommandClass {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveMultiAssociationCommandClass.class);

    private static final int MULTI_INSTANCE_MARKER = 0x00;
    private static final int MULTI_ASSOCIATIONCMD_SET = 0x01;
    private static final int MULTI_ASSOCIATIONCMD_GET = 0x02;
    private static final int MULTI_ASSOCIATIONCMD_REPORT = 0x03;
    private static final int MULTI_ASSOCIATIONCMD_REMOVE = 0x04;
    private static final int MULTI_ASSOCIATIONCMD_GROUPINGSGET = 0x05;
    private static final int MULTI_ASSOCIATIONCMD_GROUPINGSREPORT = 0x06;

    // Stores the list of association groups
    private Map<Integer, ZWaveAssociationGroup> configAssociations = new HashMap<Integer, ZWaveAssociationGroup>();

    @XStreamOmitField
    private int updateAssociationsNode = 0;

    @XStreamOmitField
    private ZWaveAssociationGroup pendingAssociation = null;

    // This will be set when we query a node for the number of groups it supports
    private int maxGroups = 0;

    /**
     * Creates a new instance of the ZWaveMultiAssociationCommandClass class.
     *
     * @param node
     *            the node this command class belongs to
     * @param controller
     *            the controller to use
     * @param endpoint
     *            the endpoint this Command class belongs to
     */
    public ZWaveMultiAssociationCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.MULTI_INSTANCE_ASSOCIATION;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received Multi Association Request", this.getNode().getNodeId());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case MULTI_ASSOCIATIONCMD_SET:
                processAssociationReport(serialMessage, offset);
                break;
            case MULTI_ASSOCIATIONCMD_REPORT:
                processAssociationReport(serialMessage, offset);
                break;
            case MULTI_ASSOCIATIONCMD_GROUPINGSREPORT:
                processGroupingsReport(serialMessage, offset);
                return;
            default:
                logger.warn(String.format("NODE %d: Unsupported Command 0x%02X for command class %s (0x%02X).",
                        this.getNode().getNodeId(), command, this.getCommandClass().getLabel(),
                        this.getCommandClass().getKey()));
        }
    }

    /**
     * Processes a CONFIGURATIONCMD_REPORT / CONFIGURATIONCMD_SET message.
     *
     * @param serialMessage
     *            the incoming message to process.
     * @param offset
     *            the offset position from which to start message processing.
     * @throws ZWaveSerialMessageException
     */
    protected void processAssociationReport(SerialMessage serialMessage, int offset) throws ZWaveSerialMessageException {
        // Extract the group index
        int group = serialMessage.getMessagePayloadByte(offset + 1);
        // The max associations supported (0 if the requested group is not supported)
        int maxAssociations = serialMessage.getMessagePayloadByte(offset + 2);
        // Number of outstanding requests (if the group is large, it may come in multiple frames)
        int following = serialMessage.getMessagePayloadByte(offset + 3);

        if (maxAssociations == 0) {
            // Unsupported association group. Nothing to do!
            if (updateAssociationsNode == group) {
                logger.debug("NODE {}: All association groups acquired.", this.getNode().getNodeId());

                updateAssociationsNode = 0;

                // This is used for network management, so send a network event
                this.getController()
                        .notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.AssociationUpdate,
                                this.getNode().getNodeId(), ZWaveNetworkEvent.State.Success));
            }
            return;
        }

        logger.debug("NODE {}: association group {} has max associations {}", this.getNode().getNodeId(), group,
                maxAssociations);

        // Are we waiting to synchronise the start of a new group?
        if (pendingAssociation == null) {
            pendingAssociation = new ZWaveAssociationGroup(group);
        }

        if (serialMessage.getMessagePayload().length > (offset + 4)) {
            logger.debug("NODE {}: Association group {} includes the following nodes:", this.getNode().getNodeId(),
                    group);
            int dataLength = serialMessage.getMessagePayload().length - (offset + 4);
            int dataPointer = 0;

            // Process the root associations
            for (; dataPointer < dataLength; dataPointer++) {
                int node = serialMessage.getMessagePayloadByte(offset + 4 + dataPointer);
                if (node == MULTI_INSTANCE_MARKER) {
                    break;
                }
                logger.debug("NODE {}: Associated with Node {} in group {}", this.getNode().getNodeId(), node, group);

                // Add the node to the group
                pendingAssociation.addAssociation(node);
            }

            // Process the multi instance associations
            if (dataPointer < dataLength) {
                logger.trace("NODE {}: Includes multi_instance associations", this.getNode().getNodeId());

                // Step over the marker
                dataPointer++;
                for (; dataPointer < dataLength; dataPointer += 2) {
                    int node = serialMessage.getMessagePayloadByte(offset + 4 + dataPointer);
                    int endpoint = serialMessage.getMessagePayloadByte(offset + 5 + dataPointer);
                    if (node == MULTI_INSTANCE_MARKER) {
                        break;
                    }
                    logger.debug("NODE {}: Associated with Node {} endpoint {} in group", this.getNode().getNodeId(),
                            node, endpoint, group);

                    // Add the node to the group
                    pendingAssociation.addAssociation(node, endpoint);
                }
            }
        }

        // If this is the end of the group, update the list then let the listeners know
        if (following == 0) {
            // Clear the current information for this group
            configAssociations.remove(group);

            // Update the group in the list
            configAssociations.put(group, pendingAssociation);

            // Send an event to the users
            ZWaveAssociationEvent zEvent = new ZWaveAssociationEvent(this.getNode().getNodeId(), pendingAssociation);
            pendingAssociation = null;
            this.getController().notifyEventListeners(zEvent);
        }

        // Is this the end of the list
        if (following == 0 && group == updateAssociationsNode) {
            // This is the end of this group and the current 'get all groups' node
            // so we need to request the next group
            if (updateAssociationsNode < maxGroups) {
                updateAssociationsNode++;
                SerialMessage outputMessage = getAssociationMessage(updateAssociationsNode);
                if (outputMessage != null) {
                    this.getController().sendData(outputMessage);
                }
            } else {
                logger.debug("NODE {}: All association groups acquired.", this.getNode().getNodeId());
                // We have reached our maxNodes, notify listeners we are done.

                updateAssociationsNode = 0;
                // This is used for network management, so send a network event
                this.getController()
                        .notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.AssociationUpdate,
                                this.getNode().getNodeId(), ZWaveNetworkEvent.State.Success));
            }
        }
    }

    /**
     * Processes a ASSOCIATIONCMD_GROUPINGSREPORT message.
     *
     * @param serialMessage
     *            the incoming message to process.
     * @param offset
     *            the offset position from which to start message processing.
     * @throws ZWaveSerialMessageException
     */
    protected void processGroupingsReport(SerialMessage serialMessage, int offset) throws ZWaveSerialMessageException {
        maxGroups = serialMessage.getMessagePayloadByte(offset + 1);
        logger.debug("NODE {} processGroupingsReport number of groups {}", getNode(), maxGroups);
        // Start the process to query these nodes
        updateAssociationsNode = 1;
        configAssociations.clear();
        SerialMessage sm = getAssociationMessage(updateAssociationsNode);
        if (sm != null) {
            this.getController().sendData(sm);
        }
    }

    /**
     * Gets a SerialMessage with the MULTI_ASSOCIATIONCMD_SET command
     *
     * @param group
     *            the association group
     * @param node
     *            the node to add to the specified group
     * @return the serial message
     */
    public SerialMessage setAssociationMessage(int group, int node, int endpoint) {
        logger.debug("NODE {}: Creating new message for command MULTI_ASSOCIATIONCMD_SET", this.getNode().getNodeId());
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.SendData, SerialMessagePriority.Set);

        if (endpoint == 0) {
            logger.trace("NODE {}: Endpoint is 0. Sending only node.", this.getNode().getNodeId());
            byte[] newPayload = { (byte) this.getNode().getNodeId(), 4, (byte) getCommandClass().getKey(),
                    (byte) MULTI_ASSOCIATIONCMD_SET, (byte) (group & 0xff), (byte) (node & 0xff) };
            result.setMessagePayload(newPayload);
        } else {
            logger.trace("NODE {}: Endpoint not 0. Sending node and endpoint.", this.getNode().getNodeId());
            byte[] newPayload = { (byte) this.getNode().getNodeId(), 6, (byte) getCommandClass().getKey(),
                    (byte) MULTI_ASSOCIATIONCMD_SET, (byte) (group & 0xff), 0, (byte) (node & 0xff),
                    (byte) (endpoint & 0xff) };
            result.setMessagePayload(newPayload);
        }

        return result;
    }

    /**
     * Gets a SerialMessage with the MULTI_ASSOCIATIONCMD_REMOVE command
     *
     * @param group
     *            the association group
     * @param node
     *            the node to add to the specified group
     * @return the serial message
     */
    public SerialMessage removeAssociationMessage(int group, int node, int endpoint) {
        logger.debug("NODE {}: Creating new message for command MULTI_ASSOCIATIONCMD_REMOVE",
                this.getNode().getNodeId());
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.SendData, SerialMessagePriority.Set);

        byte[] newPayload;
        if (endpoint == 0) {
            logger.trace("NODE {}: Endpoint is 0. Sending only node.", this.getNode().getNodeId());
            newPayload = new byte[] { (byte) this.getNode().getNodeId(), 4, (byte) getCommandClass().getKey(),
                    (byte) MULTI_ASSOCIATIONCMD_REMOVE, (byte) (group & 0xff), (byte) (node & 0xff) };
        } else {
            logger.trace("NODE {}: Endpoint not 0. Sending node and endpoint.", this.getNode().getNodeId());
            newPayload = new byte[] { (byte) this.getNode().getNodeId(), 6, (byte) getCommandClass().getKey(),
                    (byte) MULTI_ASSOCIATIONCMD_REMOVE, (byte) (group & 0xff), 0, (byte) (node & 0xff),
                    (byte) (endpoint & 0xff) };
        }
        result.setMessagePayload(newPayload);
        return result;
    }

    /**
     * Gets a SerialMessage with the MULTI_ASSOCIATIONCMD_GET command
     *
     * @param group
     *            the association group to read
     * @return the serial message
     */
    public SerialMessage getAssociationMessage(int group) {
        logger.debug("NODE {}: Creating new message for command MULTI_ASSOCIATIONCMD_GET group {}",
                this.getNode().getNodeId(), group);
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        byte[] newPayload = { (byte) this.getNode().getNodeId(), 3, (byte) getCommandClass().getKey(),
                (byte) MULTI_ASSOCIATIONCMD_GET, (byte) (group & 0xff) };
        result.setMessagePayload(newPayload);
        return result;
    }

    /**
     * Gets a SerialMessage with the MULTI_ASSOCIATIONCMD_GROUPINGSGET command
     *
     * @return the serial message
     */
    public SerialMessage getGroupingsMessage() {
        logger.debug("NODE {}: Creating new message for command MULTI_ASSOCIATIONCMD_GROUPINGSGET",
                this.getNode().getNodeId());
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        byte[] newPayload = { (byte) this.getNode().getNodeId(), 2, (byte) getCommandClass().getKey(),
                (byte) MULTI_ASSOCIATIONCMD_GROUPINGSGET };
        result.setMessagePayload(newPayload);
        return result;
    }

    /**
     * Request all association groups.
     * This method requests the number of groups from a node, when that
     * replay is processed we request association group 1 and set flags so that
     * when the response is received the command class automatically
     * requests the next group. This continues until we reach the maximum
     * number of group the device reports to us or until the device returns
     * a group with no members.
     */
    public void getAllAssociations() {
        SerialMessage serialMessage = getGroupingsMessage();
        if (serialMessage != null) {
            this.getController().sendData(serialMessage);
        }
    }

    /**
     * Returns a list of nodes that are currently members of the association
     * group. This method only returns the list that is currently in the
     * class - it does not interact with the device.
     *
     * To update the list stored in the class, call getAssociationMessage
     *
     * @param group
     *            number of the association group
     * @return List of nodes in the group
     */
    public ZWaveAssociationGroup getGroupMembers(int group) {
        return configAssociations.get(group);
    }

    /**
     * Returns the number of association groups
     *
     * @return Number of association groups
     */
    public int getGroupCount() {
        return configAssociations.size();
    }
}
