package org.openhab.binding.mysensors.internal.sensors;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.mysensors.internal.Pair;
import org.openhab.binding.mysensors.internal.sensors.type.MySensorsType;

public class MySensorsVariable {

    private int variableType;

    private int variableNum;

    private State value;

    private MySensorsType type;

    public MySensorsVariable(Pair<Integer> variableTypeAndNumber, MySensorsType type) {
        setValue(UnDefType.UNDEF);
        setType(type);
        setVariableNum(variableNum);
    }

    public MySensorsVariable(int variableNum, MySensorsType type, State state) {
        setValue(state);
        setType(type);
        setVariableNum(variableNum);
    }

    public int getVariableNum() {
        return variableNum;
    }

    public void setVariableNum(int variableNum) {
        this.variableNum = variableNum;
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
        setValue(type.fromCommand(value));
    }

    public void setValue(String value) throws Throwable {
        setValue(type.fromString(value));
    }

    public MySensorsType getType() {
        return type;
    }

    public void setType(MySensorsType type) {
        this.type = type;
    }

    public int getVariableType() {
        return variableType;
    }

    public void setVariableType(int variableType) {
        this.variableType = variableType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + variableNum;
        result = prime * result + variableType;
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
        if (variableNum != other.variableNum) {
            return false;
        }
        if (variableType != other.variableType) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MySensorsVariable [variableType=" + variableType + ", variableNum=" + variableNum + ", value=" + value
                + ", type=" + type + "]";
    }

}
