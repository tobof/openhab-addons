package org.openhab.binding.mysensors.internal.sensors;

import java.util.Date;

import org.openhab.binding.mysensors.internal.exception.RevertVariableStateException;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;

public class MySensorsVariable {

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

}
