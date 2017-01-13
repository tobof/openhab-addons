package org.openhab.binding.mysensors.internal.handler;

import java.util.List;

import org.openhab.binding.mysensors.internal.event.MySensorsGatewayEventListener;
import org.openhab.binding.mysensors.internal.factory.MySensorsCacheFactory;
import org.openhab.binding.mysensors.internal.gateway.MySensorsGateway;
import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;

public class MySensorsCacheUpdateHandler implements MySensorsGatewayEventListener {

    private MySensorsGateway myGateway;

    public MySensorsCacheUpdateHandler(MySensorsGateway myGateway) {
        this.myGateway = myGateway;
    }

    private void updateCacheFile() {
        MySensorsCacheFactory cacheFactory = MySensorsCacheFactory.getCacheFactory();

        List<Integer> givenIds = myGateway.getGivenIds();

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
    public void connectionStatusUpdate(MySensorsAbstractConnection connection, boolean connected) throws Throwable {
        updateCacheFile();
    }
}
