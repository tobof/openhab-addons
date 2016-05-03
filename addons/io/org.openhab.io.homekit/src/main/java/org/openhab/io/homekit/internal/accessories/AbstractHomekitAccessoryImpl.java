/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beowulfe.hap.HomekitAccessory;

/**
 * Abstract class for HomekitAccessory implementations, this provides the
 * accessory metadata using information from the underlying Item.
 *
 * @author Andy Lintner
 */
abstract class AbstractHomekitAccessoryImpl<T extends GenericItem> implements HomekitAccessory {

    private final int accessoryId;
    private final String itemName;
    private final String itemLabel;
    private final ItemRegistry itemRegistry;
    private final HomekitAccessoryUpdater updater;

    private Logger logger = LoggerFactory.getLogger(AbstractHomekitAccessoryImpl.class);

    public AbstractHomekitAccessoryImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater, Class<T> expectedItemClass) {
        this.accessoryId = taggedItem.getId();
        this.itemName = taggedItem.getItem().getName();
        this.itemLabel = taggedItem.getItem().getLabel();
        this.itemRegistry = itemRegistry;
        this.updater = updater;
        if (expectedItemClass != taggedItem.getItem().getClass()
                && expectedItemClass.isAssignableFrom(taggedItem.getItem().getClass())) {
            logger.error("Type " + taggedItem.getItem().getName() + " is a " + taggedItem.getItem().getClass().getName()
                    + " instead of the expected " + expectedItemClass.getName());
        }
    }

    @Override
    public int getId() {
        return accessoryId;
    }

    @Override
    public String getLabel() {
        return itemLabel;
    }

    @Override
    public String getManufacturer() {
        return "none";
    }

    @Override
    public String getModel() {
        return "none";
    }

    @Override
    public String getSerialNumber() {
        return "none";
    }

    @Override
    public void identify() {
        // We're not going to support this for now
    }

    protected ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    protected String getItemName() {
        return itemName;
    }

    protected HomekitAccessoryUpdater getUpdater() {
        return updater;
    }

    @SuppressWarnings("unchecked")
    protected T getItem() {
        return (T) getItemRegistry().get(getItemName());
    }
}
