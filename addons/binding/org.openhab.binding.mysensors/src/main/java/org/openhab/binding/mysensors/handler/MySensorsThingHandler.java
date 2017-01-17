/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.handler;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.mysensors.adapter.MySensorsTypeAdapter;
import org.openhab.binding.mysensors.config.MySensorsSensorConfiguration;
import org.openhab.binding.mysensors.internal.event.MySensorsGatewayEventListener;
import org.openhab.binding.mysensors.internal.gateway.MySensorsGateway;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MySensorsThingHandler} is responsible for handling commands, which are
 * sent to one of the channels and messages received via the MySensors network.
 *
 * @author Tim Oberföll
 */
public class MySensorsThingHandler extends BaseThingHandler implements MySensorsGatewayEventListener {

    private Logger logger = LoggerFactory.getLogger(MySensorsThingHandler.class);

    private MySensorsSensorConfiguration configuration = null;

    private int nodeId = 0;
    private int childId = 0;
    private boolean requestAck = false;
    private boolean revertState = true;

    private boolean smartSleep = false;

    private DateTimeType lastUpdate = null;

    private Map<Integer, String> oldMsgContent = new HashMap<Integer, String>();

    private MySensorsGateway myGateway;

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
        smartSleep = configuration.smartSleep;

        myGateway = getBridgeHandler().getMySensorsGateway();

        smartSleep = configuration.smartSleep;
        logger.debug("Configuration: nodeId {}, chiledId: {}, requestAck: {}, revertState: {}, smartSleep: {}", nodeId,
                childId, requestAck, revertState, smartSleep);

        registerListeners();

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("MySensors Bridge Status updated to {} for device: {}", bridgeStatusInfo.getStatus().toString(),
                getThing().getUID().toString());
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)
                || bridgeStatusInfo.getStatus().equals(ThingStatus.OFFLINE)) {
            registerListeners();

            // the node has the same status of the bridge
            updateStatus(bridgeStatusInfo.getStatus());
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.debug("Configuation update fo thing {}-{}: {}", nodeId, childId, configurationParameters);
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command {} received for channel uid {}", command, channelUID);
        /*
         * We don't handle refresh commands yet
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
                MySensorsMessage msg = MySensorsMessage.parse(stringTypeMessage.toString());
                myGateway.sendMessage(msg);
                return;
            }
        } else {
            MySensorsVariable var = myGateway.getVariable(nodeId, childId, INVERSE_CHANNEL_MAP.get(channelUID.getId()));
            if (var != null) {

                // Update value into the MS device
                var.setValue(loadAdapterForChannel(channelUID.getId()).fromCommand(command));

                // Create the real message to send
                MySensorsMessage newMsg = new MySensorsMessage(nodeId, childId, MySensorsMessage.MYSENSORS_MSG_TYPE_SET,
                        int_requestack, revertState, smartSleep);

                newMsg.setSubType(var.getType());
                newMsg.setMsg(var.reqValue());

                String oldPayload = oldMsgContent.get(subType);
                if (oldPayload == null) {
                    oldPayload = "";
                }
                newMsg.setOldMsg(oldPayload);
                oldMsgContent.put(subType, msgPayload);

                myGateway.sendMessage(newMsg);

            } else {
                logger.warn("Variable not found, cannot handle command for thing {}", thing.getUID());
            }
        }
    }

    @Override
    public void messageReceived(MySensorsMessage message) throws Throwable {
        handleIncomingMessageEvent(message);

    }

    @Override
    public void channelUpdateEvent(MySensorsNode node, MySensorsChild child, MySensorsVariable var) {
        if (node.getNodeId() == nodeId && child.getChildId() == childId) {
            handleChildUpdateEvent(var);
            updateLastUpdate();
        }
    }

    @Override
    public void nodeReachStatusChanged(MySensorsNode node, boolean reach) {
        // TODO Network Sanity Checker could put node to 'unreachable' causing, here, to set this thing to
        // OFFLINE, by now thing reachability depends only on bridgeStatusChanged method

    }

    /**
     * For every thing there is a lastUpdate channel in which the date/time is stored
     * a message was received from this thing.
     */
    private void updateLastUpdate() {
        // Don't always fire last update channel, do it only after a minute by
        if (lastUpdate == null || (System.currentTimeMillis() > (lastUpdate.getCalendar().getTimeInMillis() + 60000))) {
            DateTimeType dt = new DateTimeType();
            lastUpdate = dt;
            updateState(CHANNEL_LAST_UPDATE, dt);
            logger.debug("Setting last update for node/child {}/{} to {}", nodeId, childId, dt.toString());
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
        String channelName = CHANNEL_MAP.get(var.getType());
        State newState = loadAdapterForChannel(channelName).stateFromChannel(var);
        logger.debug("Updating channel: {}({}) value to: {}", channelName, var.getType(), newState);
        updateState(channelName, newState);

    }

    private MySensorsTypeAdapter loadAdapterForChannel(String channelName) {
        try {
            return TYPE_MAP.get(channelName).newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * If a new message is received via the MySensors bridge it is handed over to the ThingHandler
     * and processed in this method. After parsing the message the corresponding channel is updated.
     *
     * @param msg The message that was received by the MySensors gateway.
     */
    private void handleIncomingMessageEvent(MySensorsMessage msg) {
        // Am I the all knowing node that receives all messages?
        if (nodeId == MYSENSORS_NODE_ID_ALL_KNOWING && childId == MYSENSORS_CHILD_ID_ALL_KNOWING) {
            updateState(CHANNEL_MYSENSORS_MESSAGE,
                    new StringType(MySensorsMessage.generateAPIString(msg).replaceAll("(\\r|\\n)", "")));

        }
    }

    private void registerListeners() {
        if (!myGateway.isEventListenerRegisterd(this)) {
            logger.debug("Event listener for node {}-{} not registered yet, registering...", nodeId, childId);
            myGateway.addEventListener(this);
        }
    }

    @Override
    public String toString() {
        return "MySensorsThingHandler [nodeId=" + nodeId + ", childId=" + childId + "]";
    }

}
