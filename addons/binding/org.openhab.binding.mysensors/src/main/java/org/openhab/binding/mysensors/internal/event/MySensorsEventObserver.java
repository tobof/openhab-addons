package org.openhab.binding.mysensors.internal.event;

import java.util.EventListener;
import java.util.Iterator;

public interface MySensorsEventObserver<T extends EventListener> {

    public boolean isEventListenerRegisterd(T listener);

    /**
     * @param listener An Object, that wants to listen on status updates
     */
    public void addEventListener(T listener);

    public void removeEventListener(T listener);

    public void clearAllListeners();

    public Iterator<T> getEventListenersIterator();
}
