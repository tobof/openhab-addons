package org.openhab.binding.mysensors.internal.event;

import java.util.EventListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventRegister<T extends EventListener> implements MySensorsEventObservable<T> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private List<T> registeredEventListener;

    public EventRegister() {
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

        if (listener == null) {
            return;
        }

        synchronized (registeredEventListener) {
            if (!isEventListenerRegisterd(listener)) {
                logger.trace("Adding listener {} to {}", listener, this);
                registeredEventListener.add(listener);
            } else {
                logger.debug("Event listener {} already registered", listener);
            }
        }
    }

    @Override
    public void removeEventListener(T listener) {

        if (listener == null) {
            return;
        }

        // Thread-safe remove
        synchronized (registeredEventListener) {
            if (isEventListenerRegisterd(listener)) {
                logger.trace("Removing listener {} from {}", listener, this);
                Iterator<T> iter = registeredEventListener.iterator();
                while (iter.hasNext()) {
                    T elem = iter.next();
                    if (elem.equals(listener)) {
                        iter.remove();
                        return;
                    }
                }
            } else {
                logger.debug("Listener {} not present, cannot remove it", listener);
            }
        }
    }

    @Override
    public void clearAllListeners() {
        logger.trace("Clearing all listeners from {}", this);
        synchronized (registeredEventListener) {
            registeredEventListener.clear();
        }

    }

    @Override
    public List<T> getEventListeners() {
        return registeredEventListener;
    }

}