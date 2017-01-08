package org.openhab.binding.mysensors.internal.handler;

import java.util.List;

import org.openhab.binding.mysensors.internal.event.MySensorsBridgeConnectionEventListener;
import org.openhab.binding.mysensors.internal.event.MySensorsDeviceEventListener;
import org.openhab.binding.mysensors.internal.factory.MySensorsCacheFactory;
import org.openhab.binding.mysensors.internal.protocol.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.sensors.MySensorsDeviceManager;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;

public class MySensorsCacheUpdateHandler
        implements MySensorsDeviceEventListener, MySensorsBridgeConnectionEventListener {

    private MySensorsDeviceManager deviceManager;

    public MySensorsCacheUpdateHandler(MySensorsDeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    private void updateCacheFile() {
        MySensorsCacheFactory cacheFactory = MySensorsCacheFactory.getCacheFactory();

        List<Integer> givenIds = deviceManager.getGivenIds();

        cacheFactory.writeCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE, givenIds.toArray(new Integer[] {}),
                Integer[].class);
    }

    @Override
    public void nodeIdReservationDone(Integer reservedId) throws Throwable {
        updateCacheFile();
    }

    @Override
    public void newNodeDiscovered(MySensorsNode message) throws Throwable {
        updateCacheFile();
    }

    @Override
    public void bridgeStatusUpdate(MySensorsBridgeConnection connection, boolean connected) throws Throwable {
        updateCacheFile();
    }
}
