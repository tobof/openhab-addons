package org.openhab.binding.mysensors.internal.factory;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;

public class MySensorsFactory {
    public static <T> MySensorsChild<?> createChild(int childId, int childType, T value) {
        MySensorsChild<?> child = null;

        return child;
    }
}
