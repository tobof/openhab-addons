/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.handler;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.text.SimpleDateFormat;
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
import org.openhab.binding.mysensors.config.MySensorsSensorConfiguration;
import org.openhab.binding.mysensors.converter.MySensorsTypeConverter;
import org.openhab.binding.mysensors.internal.event.MySensorsGatewayEventListener;
import org.openhab.binding.mysensors.internal.event.MySensorsNodeUpdateEventType;
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

    private Logger logger = LoggerFactory.getLogger(getClass());

    private MySensorsSensorConfiguration configuration = null;

    private int nodeId = 0;
    private int childId = 0;
    private boolean requestAck = false;
    private boolean revertState = true;

    private boolean smartSleep = false;

    private int expectUpdateTimeout = -1;

    private DateTimeType lastUpdate = null;

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
        expectUpdateTimeout = configuration.expectUpdateTimeout;

        myGateway = getBridgeHandler().getMySensorsGateway();
        addIntoGateway(getThing(), configuration);

        logger.debug(
                "Configuration: nodeId {}, chiledId: {}, requestAck: {}, revertState: {}, smartSleep: {}, expectUpdateTimeout: {}",
                nodeId, childId, requestAck, revertState, smartSleep, expectUpdateTimeout);

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
            MySensorsTypeConverter adapter = loadAdapterForChannelType(channelUID.getId());

            logger.trace("Adapter {} found for type {}", adapter.getClass().getSimpleName(), channelUID.getId());

            if (adapter != null) {
                Integer type = adapter.typeFromChannelCommand(channelUID.getId(), command);

                if (type != null) {
                    logger.trace("Type for channel: {}, command: {} of thing {} is: {}", thing.getUID(), command,
                            thing.getUID(), type);

                    MySensorsVariable var = myGateway.getVariable(nodeId, childId, type);

                    if (var != null) {

                        int subType = var.getType();

                        // Create the real message to send
                        MySensorsMessage newMsg = new MySensorsMessage(nodeId, childId,
                                MySensorsMessage.MYSENSORS_MSG_TYPE_SET, int_requestack, revertState, smartSleep);

                        newMsg.setSubType(subType);
                        newMsg.setMsg(adapter.fromCommand(command));

                        myGateway.sendMessage(newMsg);

                    } else {
                        logger.warn("Variable not found, cannot handle command for thing {} of type {}", thing.getUID(),
                                channelUID.getId());
                    }
                } else {
                    logger.error("Could not get type of variable for channel: {}, command: {} of thing {}",
                            thing.getUID(), command, thing.getUID());
                }

            } else {
                logger.error("Type adapter not found for {}", channelUID.getId());
            }

        }
    }

    @Override
    public void messageReceived(MySensorsMessage message) throws Throwable {
        handleIncomingMessageEvent(message);

    }

    @Override
    public void sensorUpdateEvent(MySensorsNode node, MySensorsChild child, MySensorsVariable var,
            MySensorsNodeUpdateEventType eventType) {
        switch (eventType) {
            case UPDATE:
            case REVERT:
                if ((node.getNodeId() == nodeId) && (child.getChildId() == childId)) {
                    handleChildUpdateEvent(var);
                    updateLastUpdate(node, eventType == MySensorsNodeUpdateEventType.REVERT);
                }
                break;
            case BATTERY:
                if (node.getNodeId() == nodeId) {
                    updateLastUpdate(node, eventType == MySensorsNodeUpdateEventType.REVERT);
                }
                break;
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
    private void updateLastUpdate(MySensorsNode node, boolean isRevert) {
        // Don't always fire last update channel, do it only after a minute by
        if (lastUpdate == null || (System.currentTimeMillis() > (lastUpdate.getCalendar().getTimeInMillis() + 60000))
                || revertState) {
            DateTimeType dt = new DateTimeType(
                    new SimpleDateFormat(DateTimeType.DATE_PATTERN).format(node.getLastUpdate()));
            lastUpdate = dt;
            updateState(CHANNEL_LAST_UPDATE, dt);
            if (!isRevert) {
                logger.debug("Setting last update for node/child {}/{} to {}", nodeId, childId, dt.toString());
            } else {
                logger.warn("Setting last update for node/child {}/{} BACK (due to revert) to {}", nodeId, childId,
                        dt.toString());
            }
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
        State newState = loadAdapterForChannelType(channelName).stateFromChannel(var);
        logger.debug("Updating channel: {}({}) value to: {}", channelName, var.getType(), newState);
        updateState(channelName, newState);

    }

    private MySensorsTypeConverter loadAdapterForChannelType(String channelName) {
        return TYPE_MAP.get(channelName);
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

    private void addIntoGateway(Thing thing, MySensorsSensorConfiguration configuration) {
        MySensorsNode node = generateNodeFromThing(thing, configuration);
        if (node != null) {
            myGateway.addNode(node, true);
        } else {
            logger.error("Failed to build sensor for thing: {}", thing.getUID());
        }
    }

    private MySensorsNode generateNodeFromThing(Thing t, MySensorsSensorConfiguration configuration) {
        MySensorsNode ret = null;
        Integer nodeId = -1, childId = -1, pres = -1;
        try {
            nodeId = Integer.parseInt(t.getConfiguration().as(MySensorsSensorConfiguration.class).nodeId);
            childId = Integer.parseInt(t.getConfiguration().as(MySensorsSensorConfiguration.class).childId);
            pres = INVERSE_THING_UID_MAP.get(t.getThingTypeUID());

            if (pres != null) {
                logger.trace("Building sensors from thing: {}, node: {}, child: {}, presentation: {}", t.getUID(),
                        nodeId, childId, pres);

                MySensorsChild child = MySensorsChild.fromPresentation(pres, childId);
                if (child != null) {
                    ret = new MySensorsNode(nodeId);
                    ret.addChild(child);
                }
            } else {
                logger.error("Error on building sensors from thing: {}, node: {}, child: {}, presentation: {}",
                        t.getUID(), nodeId, childId, pres);
            }

        } catch (Exception e) {
            logger.error("Failing on create node/child for thing {}", thing.getUID(), e);
        }

        return ret;

    }

    @Override
    public String toString() {
        return "MySensorsThingHandler [nodeId=" + nodeId + ", childId=" + childId + "]";
    }

}
