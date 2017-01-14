package org.openhab.binding.mysensors.internal.sensors;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.mysensors.internal.Pair;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.sensors.type.MySensorsType;

public class MySensorsChannel {

    private Pair<Integer> variableTypeAndNumber;

    private State value;

    private MySensorsType type;

    public MySensorsChannel(Pair<Integer> variableTypeAndNumber, MySensorsType type) {
        setValue(UnDefType.UNDEF);
        setType(type);
        setVariableTypeAndNumber(variableTypeAndNumber);
    }

    public synchronized String getPayloadValue() {
        return getType().toPayloadString(getValue());
    }

    public synchronized int getSubtypeValue() {
        Integer subType = getType().toSubtypeInt(getValue());
        if (subType != null) {
            return subType;
        } else {
            return variableTypeAndNumber.getSecond();
        }
    }

    public synchronized State getValue() {
        return value;
    }

    public synchronized void setValue(State value) {
        if (value == null) {
            throw new NullPointerException("Cannot have state to null. Use UnDefType instead");
        }
        this.value = value;
    }

    public void setValue(Command value) {
        setValue(getType().fromCommand(value));
    }

    public void setValue(MySensorsMessage value) throws Throwable {
        setValue(getType().fromMessage(value));
    }

    public synchronized MySensorsType getType() {
        return type;
    }

    public synchronized void setType(MySensorsType type) {
        this.type = type;
    }

    public Pair<Integer> getVariableTypeAndNumber() {
        return variableTypeAndNumber;
    }

    public void setVariableTypeAndNumber(Pair<Integer> variableTypeAndNumber) {
        this.variableTypeAndNumber = variableTypeAndNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + ((variableTypeAndNumber == null) ? 0 : variableTypeAndNumber.hashCode());
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
        MySensorsChannel other = (MySensorsChannel) obj;
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        if (variableTypeAndNumber == null) {
            if (other.variableTypeAndNumber != null) {
                return false;
            }
        } else if (!variableTypeAndNumber.equals(other.variableTypeAndNumber)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MySensorsVariable [variableTypeAndNumber=" + variableTypeAndNumber + ", value=" + value + ", type="
                + type + "]";
    }

}
