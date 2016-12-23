package org.openhab.binding.mysensors.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MySensorsUtility {
    public static <V, K> Map<V, K> invertMap(Map<K, V> map, boolean hasDuplicate) throws NullPointerException {
        if (!hasDuplicate) {
            return map.entrySet().stream().collect(Collectors.toMap(Entry::getValue, c -> c.getKey()));
        } else {
            return map.entrySet().stream().collect(Collectors.toMap(Entry::getValue, c -> c.getKey(), (a, b) -> a));
        }
    }

    public static <K, V> Map<K, V> joinMap(Map<K, V> map1, Map<K, V> map2) throws NullPointerException {
        HashMap<K, V> joinMap = new HashMap<K, V>();
        joinMap.putAll(map1);
        joinMap.putAll(map2);
        return joinMap;
    }

    public static <K, V> void mergeMap(Map<K, V> map1, Map<K, V> map2) throws NullPointerException {
        map1.putAll(map2);
    }
}
