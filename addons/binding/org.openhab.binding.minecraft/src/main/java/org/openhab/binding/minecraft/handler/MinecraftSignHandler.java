/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.minecraft.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.minecraft.MinecraftBindingConstants;
import org.openhab.binding.minecraft.config.SignConfig;
import org.openhab.binding.minecraft.message.data.SignData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Subscription;

/**
 * The {@link MinecraftSignHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mattias Markehed - Initial contribution
 */
public class MinecraftSignHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(MinecraftSignHandler.class);

    private MinecraftServerHandler bridgeHandler;
    private Subscription signSubscription;
    private SignConfig config;

    public MinecraftSignHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        this.bridgeHandler = getBridgeHandler();
        this.config = getThing().getConfiguration().as(SignConfig.class);

        if (getThing().getBridgeUID() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");

            return;
        }

        updateStatus(ThingStatus.ONLINE);
        hookupListeners(bridgeHandler);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (!signSubscription.isUnsubscribed()) {
            signSubscription.unsubscribe();
        }
    }

    private void hookupListeners(MinecraftServerHandler bridgeHandler) {
        signSubscription = bridgeHandler.getSignsRx().flatMap(signs -> Observable.from(signs))
                .filter(sign -> config.getName().equals(sign.getName())).subscribe(sign -> updateSignState(sign));
    }

    /**
     * Updates sign state of player.
     *
     * @param sign the sign to update
     */
    private void updateSignState(SignData sign) {
        State activeState = sign.getState() ? OnOffType.ON : OnOffType.OFF;
        updateState(MinecraftBindingConstants.CHANNEL_SIGN_ACTIVE, activeState);
    }

    private synchronized MinecraftServerHandler getBridgeHandler() {

        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Required bridge not defined for device {}.");
            return null;
        } else {
            return getBridgeHandler(bridge);
        }
    }

    private synchronized MinecraftServerHandler getBridgeHandler(Bridge bridge) {

        MinecraftServerHandler bridgeHandler = null;

        ThingHandler handler = bridge.getHandler();
        if (handler instanceof MinecraftServerHandler) {
            bridgeHandler = (MinecraftServerHandler) handler;
        } else {
            logger.debug("No available bridge handler found yet. Bridge: {} .", bridge.getUID());
            bridgeHandler = null;
        }
        return bridgeHandler;
    }

    @Override
    public void updateState(String channelID, State state) {
        ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), channelID);
        updateState(channelUID, state);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}