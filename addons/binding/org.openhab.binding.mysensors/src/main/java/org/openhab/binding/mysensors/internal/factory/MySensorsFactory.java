package org.openhab.binding.mysensors.internal.factory;

import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;

public class MySensorsFactory {
    public static <T> MySensorsChild<T> createChild(int childId, int childType, T value) {
        MySensorsChild<T> child = null;

        child = new MySensorsChild<T>(childId, value);

        return child;
    }
}
