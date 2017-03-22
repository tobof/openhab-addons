/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.sensors;

import java.util.Date;

import org.openhab.binding.mysensors.internal.exception.RevertVariableStateException;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;

/**
 * Variables (states) of a MySensors child.
 * 
 * @author Tim Oberföll
 * @author Andrea Cioni
 *
 */
public abstract class MySensorsVariable {

    private final int type;

    private String value;

    private Date lastUpdate;

    private String oldState;

    private Date oldLastUpdate;

    public MySensorsVariable(int type) {
        this.type = type;
    }

    public synchronized String getValue() {
        return value;
    }

    public synchronized void setValue(String value) {
        oldState = getValue();
        oldLastUpdate = getLastUpdate();
        setLastUpdate(new Date());
        this.value = value;
    }

    public synchronized void setValue(MySensorsMessage message) {
        setValue(message.getMsg());
    }

    public synchronized int getType() {
        return type;
    }

    public synchronized Date getLastUpdate() {
        return lastUpdate;
    }

    public synchronized void setLastUpdate(Date lastupdate) {
        this.lastUpdate = lastupdate;
    }

    public synchronized boolean isRevertible() {
        return (oldState != null && oldLastUpdate != null);
    }

    public synchronized void revertValue() throws RevertVariableStateException {
        if (isRevertible()) {
            setValue(oldState);
            setLastUpdate(oldLastUpdate);
            oldState = null;
            oldLastUpdate = null;
        } else {
            throw new RevertVariableStateException();
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + type;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MySensorsVariable other = (MySensorsVariable) obj;
        if (type != other.type) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [value=" + value + "]";
    }

}
