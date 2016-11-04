package org.openhab.binding.mysensors.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MySensorsUtility {
    public static <V, K> Map<V, K> invertMap(Map<K, V> map) throws NullPointerException {
        return map.entrySet().stream().collect(Collectors.toMap(Entry::getValue, c -> c.getKey()));
    }

    public static <K, V> Map<K, V> joinMap(Map<K, V> map1, Map<K, V> map2) throws NullPointerException {
        HashMap<K, V> joinMap = new HashMap<K, V>();
        joinMap.putAll(map1);
        joinMap.putAll(map2);
        return joinMap;
    }
}
