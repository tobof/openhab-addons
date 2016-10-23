package org.openhab.binding.mysensors.internal.sensors;

import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.mysensors.internal.sensors.type.MySensorsType;

public class MySensorsVariable {

    private int variableNum;

    private State value;

    private MySensorsType type;

    public MySensorsVariable(int variableNum, MySensorsType type) {
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

    public State getValue() {
        return value;
    }

    public void setValue(State value) {
        if (value == null) {
            throw new NullPointerException("Cannot have state to null. Use UnDefType instead");
        }
        this.value = value;
    }

    public void setValue(String value) throws Throwable {
        setValue(type.fromString(value));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + variableNum;
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
        return true;
    }

    @Override
    public String toString() {
        return "MySensorsVariable [variableNum=" + variableNum + ", value=" + value + "]";
    }

    public MySensorsType getType() {
        return type;
    }

    public void setType(MySensorsType type) {
        this.type = type;
    }

}
