package org.openhab.binding.mysensors.internal.sensors;

import java.util.Date;

import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;

public class MySensorsVariable {

    private int type;

    private String value;

    private Date lastUpdate;

    public MySensorsVariable(int type) {
        setType(type);
    }

    public synchronized String reqValue() {
        return value;
    }

    public synchronized void setValue(String value) {
        setLastUpdate(new Date());
        this.value = value;
    }

    public synchronized void setValue(MySensorsMessage message) {
        setValue(message.msg);
    }

    public synchronized void setType(int type) {
        this.type = type;
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

}
