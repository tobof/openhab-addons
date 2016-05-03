/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.homekit.internal.accessories;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.accessories.DimmableLightbulb;

/**
 * Implements DimmableLightBulb using an Item that provides a On/Off and Percent state.
 *
 * @author Andy Lintner
 */
class HomekitDimmableLightbulbImpl extends AbstractHomekitLightbulbImpl<DimmerItem>implements DimmableLightbulb {

    public HomekitDimmableLightbulbImpl(HomekitTaggedItem taggedItem, ItemRegistry itemRegistry,
            HomekitAccessoryUpdater updater) {
        super(taggedItem, itemRegistry, updater, DimmerItem.class);
    }

    @Override
    public CompletableFuture<Integer> getBrightness() {
        PercentType state = (PercentType) getItem().getStateAs(PercentType.class);
        return CompletableFuture.completedFuture(state.intValue());
    }

    @Override
    public CompletableFuture<Void> setBrightness(Integer value) throws Exception {
        getItem().send(new PercentType(value));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeBrightness(HomekitCharacteristicChangeCallback callback) {
        getUpdater().subscribe(getItem(), "brightness", callback);
    }

    @Override
    public void unsubscribeBrightness() {
        getUpdater().unsubscribe(getItem(), "brightness");
    }

}
