package org.openhab.binding.mysensors.adapter;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;

public class MySensorsDecimalTypeAdapter extends DecimalType implements MySensorsTypeAdapter {

    /**
     *
     */
    private static final long serialVersionUID = 7773038279703304405L;

    @Override
    public State fromString(String s) {
        return new DecimalType(s);
    }
}
