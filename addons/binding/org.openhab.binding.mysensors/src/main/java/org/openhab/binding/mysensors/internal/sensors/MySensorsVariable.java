package org.openhab.binding.mysensors.internal.sensors;

import java.util.Date;

import org.openhab.binding.mysensors.internal.Pair;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;

public class MySensorsVariable {

    private Pair<Integer> commandAndType;

    private String value;

    private Date lastUpdate;

    public MySensorsVariable(Pair<Integer> commandAndType) {
        setCommandAndType(commandAndType);
    }

    public synchronized String getValue() {
        return value;
    }

    public synchronized void setValue(String value) {
        this.value = value;
    }

    public void setValue(MySensorsMessage message) {
        setValue(message.msg);
    }

    public synchronized void setCommandAndType(Pair<Integer> commandAndType) {
        this.commandAndType = commandAndType;
    }

    public synchronized Date getLastUpdate() {
        return lastUpdate;
    }

    public synchronized void setLastUpdate(Date lastupdate) {
        this.lastUpdate = lastupdate;
    }

    public synchronized int getCommand() {
        return commandAndType.getFirst();
    }

    public synchronized int getType() {
        return commandAndType.getSecond();
    }

}
