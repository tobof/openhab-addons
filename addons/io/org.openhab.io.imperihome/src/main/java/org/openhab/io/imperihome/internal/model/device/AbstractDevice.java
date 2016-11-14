/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.model.device;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.StateChangeListener;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.io.imperihome.internal.action.Action;
import org.openhab.io.imperihome.internal.action.ActionRegistry;
import org.openhab.io.imperihome.internal.model.param.DeviceParam;
import org.openhab.io.imperihome.internal.model.param.DeviceParameters;
import org.openhab.io.imperihome.internal.model.param.ParamType;
import org.openhab.io.imperihome.internal.processor.DeviceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract parent of all devices. Sets up and tears down state listeners and contains parameter and link data.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public abstract class AbstractDevice implements StateChangeListener {

    protected transient final Logger logger = LoggerFactory.getLogger(getClass());

    private String id;
    private String name;
    private String room;
    private DeviceType type;
    private boolean inverted;
    private final DeviceParameters params;

    private transient String roomName;
    private transient Item item;

    private final transient Map<String, String> links;
    private transient Map<String, String> mapping;

    private transient DeviceRegistry deviceRegistry;
    private transient ActionRegistry actionRegistry;

    public AbstractDevice(DeviceType type, Item item) {
        this.type = type;
        this.item = item;
        params = new DeviceParameters();
        links = new HashMap<>();

        if (item instanceof GenericItem) {
            ((GenericItem) item).addStateChangeListener(this);
        }
    }

    public void destroy() {
        if (item instanceof GenericItem) {
            ((GenericItem) item).removeStateChangeListener(this);
        }

        deviceRegistry = null;
        actionRegistry = null;
        item = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public DeviceParameters getParams() {
        return params;
    }

    public void addParam(DeviceParam param) {
        logger.trace("Setting param for device {}: {}", this, param);
        params.set(param);
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void addLink(String linkType, String deviceId) {
        links.put(linkType, deviceId);
    }

    public void setMapping(Map<String, String> mapping) {
        this.mapping = mapping;
    }

    public Map<String, String> getMapping() {
        return mapping;
    }

    public void setDeviceRegistry(DeviceRegistry deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
    }

    protected DeviceRegistry getDeviceRegistry() {
        return deviceRegistry;
    }

    public void setActionRegistry(ActionRegistry actionRegistry) {
        this.actionRegistry = actionRegistry;
    }

    protected ActionRegistry getActionRegistry() {
        return actionRegistry;
    }

    protected Item getItem() {
        return item;
    }

    public String getItemName() {
        return item.getName();
    }

    /**
     * Can be implemented by Devices that require their state to be updated manually, instead of relying (only) on Item
     * state change events.
     * This method is called just before serializing the device to JSON.
     */
    public void updateParams() {
        logger.trace("updateParams on {}", this);
    }

    /**
     * Performs an action on this device.
     * 
     * @param action Action name.
     * @param value Action value.
     */
    public void performAction(String action, String value) {
        Action actionInst = actionRegistry.get(action);
        if (actionInst == null) {
            logger.warn("Unknown action: {}", action);
            return;
        }

        Item item = getItem();
        if (!actionInst.supports(this, item)) {
            logger.warn("Action '{}' not supported on this device ({})", action, this);
            return;
        }

        actionInst.perform(this, item, value);
    }

    @Override
    public void stateChanged(Item item, State oldState, State newState) {
    }

    @Override
    public void stateUpdated(Item item, State newState) {
        logger.debug("Device item {} state changed to {}", item, newState);

        OnOffType onOffState = (OnOffType) item.getStateAs(OnOffType.class);
        if (onOffState != null) {
            boolean isOn = onOffState == OnOffType.ON;
            DeviceParam param = new DeviceParam(ParamType.STATUS, isOn ^ isInverted() ? "1" : "0");
            addParam(param);
        }
    }

    @Override
    public String toString() {
        return "AbstractDevice{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", room='" + room + '\'' + ", type="
                + type + ", invert=" + inverted + ", links=" + links + '}';
    }

}
