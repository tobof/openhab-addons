/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.handler;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;
import static org.openhab.binding.mysensors.internal.MySensorsUtility.invertMap;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.mysensors.config.MySensorsSensorConfiguration;
import org.openhab.binding.mysensors.internal.event.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.internal.event.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageParser;
import org.openhab.binding.mysensors.internal.sensors.MySensorsDeviceManager;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MySensorsThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tim Oberföll
 */
public class MySensorsThingHandler extends BaseThingHandler implements MySensorsUpdateListener {

    private Logger logger = LoggerFactory.getLogger(MySensorsThingHandler.class);

    private MySensorsSensorConfiguration configuration = null;

    private int nodeId = 0;
    private int childId = 0;
    private boolean requestAck = false;
    private boolean revertState = true;

    private DateTimeType lastUpdate = null;

    private Map<Integer, String> oldMsgContent = new HashMap<Integer, String>();

    private MySensorsDeviceManager deviceManager = MySensorsDeviceManager.getInstance();

    public MySensorsThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(MySensorsSensorConfiguration.class);
        nodeId = Integer.parseInt(configuration.nodeId);
        childId = Integer.parseInt(configuration.childId);
        requestAck = configuration.requestAck;
        revertState = configuration.revertState;
        logger.debug("Configuration: node {}, chiledId: {}, revertState: {}", nodeId, childId, revertState);
        if (!getBridgeHandler().getBridgeConnection().isEventListenerRegisterd(this)) {
            logger.debug("Event listener for node {}-{} not registered yet, registering...", nodeId, childId);
            getBridgeHandler().getBridgeConnection().addEventListener(this);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("MySensors Bridge Status updated to {} for device: {}", bridgeStatusInfo.getStatus().toString(),
                getThing().getUID().toString());
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)
                || bridgeStatusInfo.getStatus().equals(ThingStatus.OFFLINE)) {
            if (!getBridgeHandler().getBridgeConnection().isEventListenerRegisterd(this)) {
                logger.debug("Event listener for node {}-{} not registered yet, registering...", nodeId, childId);
                getBridgeHandler().getBridgeConnection().addEventListener(this);
            }

            // the node has the same status of the bridge
            updateStatus(bridgeStatusInfo.getStatus());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.ThingHandler#handleCommand(org.eclipse.smarthome.core.thing.ChannelUID,
     * org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        /*
         * TODO We don't handle refresh commands yet
         *
         */
        if (command == RefreshType.REFRESH) {
            return;
        }

        String msgPayload = "";
        int subType = 0;
        int int_requestack = requestAck ? 1 : 0;

        // just forward the message in case it is received via this channel. This is special!
        if (channelUID.getId().equals(CHANNEL_MYSENSORS_MESSAGE)) {
            if (command instanceof StringType) {
                StringType stringTypeMessage = (StringType) command;
                MySensorsMessage msg = MySensorsMessageParser.parse(stringTypeMessage.toString());
                getBridgeHandler().getBridgeConnection().addMySensorsOutboundMessage(msg);
                return;
            }
        } else {
            MySensorsVariable var = deviceManager.getVariable(nodeId, childId,
                    invertMap(CHANNEL_MAP, true).get(channelUID.getId()));
            if (var != null) {

                // Update value into the MS device
                var.setValue(command);

                // Create the real message to send
                MySensorsMessage newMsg = new MySensorsMessage(nodeId, childId, MYSENSORS_MSG_TYPE_SET, int_requestack,
                        revertState, subType, var.getValue().toString());

                String oldPayload = oldMsgContent.get(subType);
                if (oldPayload == null) {
                    oldPayload = "";
                }
                newMsg.setOldMsg(oldPayload);
                oldMsgContent.put(subType, msgPayload);

                getBridgeHandler().getBridgeConnection().addMySensorsOutboundMessage(newMsg);

            } else {
                logger.warn("Variable not found, cannot handle command for thing {}", thing.getUID());
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#handleUpdate(org.eclipse.smarthome.core.thing.
     * ChannelUID, org.eclipse.smarthome.core.types.State)
     */
    @Override
    public void handleUpdate(ChannelUID channelUID, org.eclipse.smarthome.core.types.State newState) {
        // logger.debug("handleUpdate called");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.mysensors.handler.MySensorsUpdateListener#statusUpdateReceived(org.openhab.binding.mysensors.
     * handler.MySensorsStatusUpdateEvent)
     */
    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {
        switch (event.getEventType()) {
            case INCOMING_MESSAGE:
                handleIncomingMessageEvent((MySensorsMessage) event.getData());
                break;
            case NODE_STATUS_UPDATE:
                // TODO Network Sanity Checker could put node to 'unreachable' causing, here, to set this thing to
                // OFFLINE
                if (!((MySensorsNode) event.getData()).isReachable()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
                break;
            case CHILD_VALUE_CHANGED:
                handleChildUpdateEvent((MySensorsVariable) event.getData());
                break;
            default:
                break;
        }
    }

    /**
     * Returns the BridgeHandler of the bridge/gateway to the MySensors network
     *
     * @return BridgeHandler of the bridge/gateway to the MySensors network
     */
    private synchronized MySensorsBridgeHandler getBridgeHandler() {
        MySensorsBridgeHandler myBridgeHandler = null;

        Bridge bridge = getBridge();
        myBridgeHandler = (MySensorsBridgeHandler) bridge.getHandler();

        return myBridgeHandler;
    }

    private void handleChildUpdateEvent(MySensorsVariable var) {
        updateState(CHANNEL_MAP.get(var.getVariableNum()), var.getValue());
    }

    private void handleIncomingMessageEvent(MySensorsMessage msg) {
        // Am I the all knowing node that receives all messages?
        if (nodeId == 999 && childId == 999) {
            updateState(CHANNEL_MYSENSORS_MESSAGE,
                    new StringType(MySensorsMessageParser.generateAPIString(msg).replaceAll("(\\r|\\n)", "")));

        }
    }
}
