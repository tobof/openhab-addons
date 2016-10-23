package org.openhab.binding.mysensors.internal;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MySensorsUtility {
    public static <V, K> Map<V, K> invertMap(Map<K, V> map) throws NullPointerException {
        return map.entrySet().stream().collect(Collectors.toMap(Entry::getValue, c -> c.getKey()));
    }
}
