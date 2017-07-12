/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.harmonyhub.handler;

import static org.openhab.binding.harmonyhub.HarmonyHubBindingConstants.HARMONY_HUB_THING_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.harmonyhub.HarmonyHubBindingConstants;
import org.openhab.binding.harmonyhub.config.HarmonyHubConfig;
import org.openhab.binding.harmonyhub.internal.HarmonyHubHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.whistlingfish.harmony.ActivityChangeListener;
import net.whistlingfish.harmony.HarmonyClient;
import net.whistlingfish.harmony.HarmonyHubListener;
import net.whistlingfish.harmony.config.Activity;
import net.whistlingfish.harmony.config.HarmonyConfig;

/**
 * The {@link HarmonyHubHandler} is responsible for handling commands for Harmony Hubs, which are
 * sent to one of the channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class HarmonyHubHandler extends BaseBridgeHandler implements HarmonyHubListener {

    private Logger logger = LoggerFactory.getLogger(HarmonyHubHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(HARMONY_HUB_THING_TYPE);

    // one minute should be plenty short, but not overwhelm the hub with requests
    private static final long CONFIG_CACHE_TIME = 60 * 1000;

    private static final int RETRY_TIME = 60;

    private static final int HEARTBEAT_INTERVAL = 30;

    private ScheduledExecutorService buttonExecutor = Executors.newSingleThreadScheduledExecutor();

    private List<HubStatusListener> listeners = new CopyOnWriteArrayList<HubStatusListener>();

    private HarmonyClient client;

    private HarmonyConfig cachedConfig;

    private Date cacheConfigExpireDate;

    private HarmonyHubHandlerFactory factory;

    private ScheduledFuture<?> retryJob;

    private ScheduledFuture<?> heartBeatJob;

    private int heartBeatInterval;

    public HarmonyHubHandler(Bridge bridge, HarmonyHubHandlerFactory factory) {
        super(bridge);
        this.factory = factory;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateState(client.getCurrentActivity());
        } else if (command instanceof StringType) {
            try {
                try {
                    int actId = Integer.parseInt(command.toString());
                    client.startActivity(actId);
                } catch (NumberFormatException ignored) {
                    client.startActivityByName(command.toString());
                }
            } catch (Exception e) {
                logger.error("Could not start activity", e);
            }
        } else if (command instanceof DecimalType) {
            try {
                client.startActivity(((DecimalType) command).intValue());
            } catch (Exception e) {
                logger.error("Could not start activity", e);
            }
        } else {
            logger.warn("Command {]: Not a acceptable type (String or Decimal), ignorning", command);
        }
    }

    @Override
    public void initialize() {
        cancelRetry();
        connect();
    }

    @Override
    public void dispose() {
        listeners.clear();
        buttonExecutor.shutdownNow();
        cancelRetry();
        disconnectFromHub();
        factory.removeChannelTypesForThing(getThing().getUID());
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail detail, String comment) {
        super.updateStatus(status, detail, comment);
        logger.debug("Updating listeners with status {}", status);
        for (HubStatusListener listener : listeners) {
            listener.hubStatusChanged(status);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (client != null) {
            updateState(channelUID, new StringType(client.getCurrentActivity().getLabel()));
        }
    }

    /**
     * HarmonyHubListener interface
     */
    @Override
    public void removeFrom(HarmonyClient hc) {
        // we have been removed from listening
    }

    /**
     * HarmonyHubListener interface
     */
    @Override
    public void addTo(HarmonyClient hc) {
        hc.addListener(new ActivityChangeListener() {
            @Override
            public void activityStarted(Activity activity) {
                updateState(activity);
            }
        });
    }

    /**
     * Starts the connection process
     */
    private synchronized void connect() {
        disconnectFromHub();

        HarmonyHubConfig config = getConfig().as(HarmonyHubConfig.class);
        heartBeatInterval = config.heartBeatInterval > 0 ? config.heartBeatInterval : HEARTBEAT_INTERVAL;

        String host = config.host;

        // earlier versions required a name and used network discovery to find the hub and retrieve the host,
        // this section is to not break that and also update older configurations to use the host configuration
        // option instead of name
        if (StringUtils.isBlank(host)) {
            host = getThing().getProperties().get(HarmonyHubBindingConstants.HUB_PROPERTY_HOST);
            if (StringUtils.isNotBlank(host)) {
                Configuration genericConfig = getConfig();
                genericConfig.put("host", host);
                updateConfiguration(genericConfig);
            } else {
                logger.debug("host not configured");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "host not configured");
                return;
            }
        }

        client = HarmonyClient.getInstance();
        client.addListener(this);

        try {
            logger.debug("Connecting: host {}", host);
            client.connect(host);
            heartBeatJob = scheduler.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    try {
                        client.sendPing();
                    } catch (Exception e) {
                        logger.warn("heartbeat failed", e);
                        setOfflineAndReconnect("Hearbeat failed");
                    }
                }
            }, heartBeatInterval, heartBeatInterval, TimeUnit.SECONDS);
            updateStatus(ThingStatus.ONLINE);
            buildChannel();
        } catch (Exception e) {
            logger.debug("Could not connect to HarmonyHub at {}", host, e);
            setOfflineAndReconnect("Could not connect: " + e.getMessage());
        }
    }

    private void disconnectFromHub() {
        if (heartBeatJob != null && !heartBeatJob.isDone()) {
            heartBeatJob.cancel(false);
        }

        if (client != null) {
            client.removeListener(this);
            client.disconnect();
        }
    }

    private void setOfflineAndReconnect(String error) {
        disconnectFromHub();
        retryJob = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }, RETRY_TIME, TimeUnit.SECONDS);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
    }

    private void cancelRetry() {
        if (retryJob != null && !retryJob.isDone()) {
            retryJob.cancel(false);
        }
    }

    private void updateState(Activity activity) {
        logger.debug("Updating current activity to {}", activity.getLabel());
        updateState(new ChannelUID(getThing().getUID(), HarmonyHubBindingConstants.CHANNEL_CURRENT_ACTIVITY),
                new StringType(activity.getLabel()));
    }

    private void buildChannel() {
        try {
            HarmonyConfig config = getCachedConfig();
            List<Activity> activities = config.getActivities();
            // sort our activities in order
            Collections.sort(activities, new Comparator<Activity>() {
                @Override
                public int compare(Activity a1, Activity a2) {
                    // if the order value is null we want it to be at the start of the list
                    int o1 = a1.getActivityOrder() == null ? -1 : a1.getActivityOrder().intValue();
                    int o2 = a2.getActivityOrder() == null ? -1 : a2.getActivityOrder().intValue();
                    return (o1 < o2) ? -1 : (o1 == o2) ? 0 : 1;
                }
            });

            // add our activities as channel state options
            List<StateOption> states = new LinkedList<StateOption>();
            for (Activity activity : activities) {
                states.add(new StateOption(activity.getLabel(), activity.getLabel()));
            }

            ChannelTypeUID channelTypeUID = new ChannelTypeUID(
                    getThing().getUID() + ":" + HarmonyHubBindingConstants.CHANNEL_CURRENT_ACTIVITY);

            ChannelType channelType = new ChannelType(channelTypeUID, false, "String", "Current Activity",
                    "Current activity for " + getThing().getLabel(), null, null,
                    new StateDescription(null, null, null, "%s", false, states), null);

            factory.addChannelType(channelType);

            BridgeBuilder thingBuilder = editThing();

            Channel channel = ChannelBuilder
                    .create(new ChannelUID(getThing().getUID(), HarmonyHubBindingConstants.CHANNEL_CURRENT_ACTIVITY),
                            "String")
                    .withType(channelTypeUID).build();

            // replace existing currentActivity with updated one
            List<Channel> currentChannels = getThing().getChannels();
            List<Channel> newChannels = new ArrayList<Channel>();
            for (Channel c : currentChannels) {
                if (!c.getUID().equals(channel.getUID())) {
                    newChannels.add(c);
                }
            }
            newChannels.add(channel);
            thingBuilder.withChannels(newChannels);

            updateThing(thingBuilder.build());
        } catch (Exception e) {
            logger.debug("Could not add current activity channel to hub", e);
        }
    }

    /**
     * Returns an instance of our connected HarmonyClient
     *
     * @return
     */
    public HarmonyClient getClient() {
        return client;
    }

    /**
     * Sends a button press to a device
     *
     * @param device
     * @param button
     */
    public void pressButton(int device, String button) {
        buttonExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (client != null) {
                    client.pressButton(device, button);
                }
            }
        });
    }

    /**
     * Sends a button press to a device
     *
     * @param device
     * @param button
     */
    public void pressButton(String device, String button) {
        buttonExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (client != null) {
                    client.pressButton(device, button);
                }
            }
        });
    }

    /**
     * Cache a call to get the harmony config so we don't overwhelm it with requests.
     *
     * @return
     */
    public synchronized HarmonyConfig getCachedConfig() {
        if (client == null) {
            return null;
        }

        Date now = new Date();
        if (cachedConfig == null || cacheConfigExpireDate == null || now.after(cacheConfigExpireDate)) {
            logger.debug("Refreshing conf cache");
            cachedConfig = client.getConfig();
            cacheConfigExpireDate = new Date(System.currentTimeMillis() + CONFIG_CACHE_TIME);
        }
        return cachedConfig;
    }

    /**
     * Adds a HubConnectedListener
     *
     * @param listener
     */
    public void addHubStatusListener(HubStatusListener listener) {
        listeners.add(listener);
        listener.hubStatusChanged(getThing().getStatus());
    }

    /**
     * Removes a HubConnectedListener
     *
     * @param listener
     */
    public void removeHubStatusListener(HubStatusListener listener) {
        listeners.remove(listener);
    }

}
