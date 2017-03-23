package org.openhab.binding.mysensors.internal;

import org.openhab.binding.mysensors.internal.exception.MergeException;

/**
 * Indicates that a class could be merged to another one of the same type
 *
 * @author Andrea Cioni
 *
 */
public interface Mergeable {

    /**
     * Merge an object to another one.
     *
     * @param o
     * @throws MergeException
     */
    public void merge(Object o) throws MergeException;
}
