package org.openhab.binding.mysensors.internal.event;

import java.util.EventListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySensorsEventRegister<T extends EventListener> implements MySensorsEventObserver<T> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private List<T> registeredEventListener;

    public MySensorsEventRegister() {
        registeredEventListener = new LinkedList<>();
    }

    @Override
    public boolean isEventListenerRegisterd(T listener) {
        boolean ret = false;
        synchronized (registeredEventListener) {
            ret = registeredEventListener.contains(listener);
        }
        return ret;
    }

    @Override
    public void addEventListener(T listener) {
        synchronized (registeredEventListener) {
            if (!isEventListenerRegisterd(listener)) {
                registeredEventListener.add(listener);
            } else {
                logger.debug("Event listener {} already registered", listener);
            }
        }
    }

    @Override
    public void removeEventListener(T listener) {
        synchronized (registeredEventListener) {
            if (isEventListenerRegisterd(listener)) {
                registeredEventListener.remove(listener);
            } else {
                logger.debug("Listener {} not present, cannot remove it", listener);
            }
        }
    }

    @Override
    public void clearAllListeners() {
        logger.debug("Clearing all listeners");
        synchronized (registeredEventListener) {
            registeredEventListener.clear();
        }

    }

    @Override
    public Iterator<T> getEventListenersIterator() {
        return registeredEventListener.iterator();
    }

}
