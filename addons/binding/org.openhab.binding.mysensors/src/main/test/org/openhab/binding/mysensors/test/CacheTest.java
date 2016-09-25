package org.openhab.binding.mysensors.test;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.mysensors.internal.factory.MySensorsCacheFactory;

public class CacheTest {

    @Test
    public void writeGivenIdsCache() {
        MySensorsCacheFactory c = MySensorsCacheFactory.getCacheFactory();
        c.writeCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE, new Number[] { 2, 3, 5 }, Number[].class);
    }

    @Test
    public void readGivenIdsCache() {
        MySensorsCacheFactory c = MySensorsCacheFactory.getCacheFactory();
        System.out.println(Arrays.toString(
                c.readCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE, new Number[] { 2, 3 }, Number[].class)));
    }

}
