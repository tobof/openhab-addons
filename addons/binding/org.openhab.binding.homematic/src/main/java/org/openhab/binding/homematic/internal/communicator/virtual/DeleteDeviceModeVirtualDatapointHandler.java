/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.VIRTUAL_DATAPOINT_NAME_DELETE_DEVICE_MODE;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmInterface;
import org.openhab.binding.homematic.internal.model.HmValueType;

/**
 * A virtual enum datapoint which holds the delete flag for deleting a device with the DELETE_DEVICE virtual datapoint.
 * Falls back to LOCKED after 30 seconds to prevent accidental deletion.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class DeleteDeviceModeVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    protected static final String MODE_LOCKED = "LOCKED";
    protected static final String MODE_RESET = "RESET";
    protected static final String MODE_FORCE = "FORCE";
    protected static final String MODE_DEFER = "DEFER";
    private static final int DELETE_MODE_DURATION = 30;

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(HmDevice device) {
        if (!device.isGatewayExtras() && !(device.getHmInterface() == HmInterface.CUXD)) {
            HmDatapoint dp = addDatapoint(device, 0, VIRTUAL_DATAPOINT_NAME_DELETE_DEVICE_MODE, HmValueType.ENUM, 0,
                    false);
            dp.setOptions(new String[] { MODE_LOCKED, MODE_RESET, MODE_FORCE, MODE_DEFER });
            dp.setMinValue(0);
            dp.setMaxValue(dp.getOptions().length - 1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandle(HmDatapoint dp, Object value) {
        return VIRTUAL_DATAPOINT_NAME_DELETE_DEVICE_MODE.equals(dp.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(VirtualGateway gateway, HmDatapoint dp, HmDatapointConfig dpConfig, Object value)
            throws IOException, HomematicClientException {
        dp.setValue(value);
        if (!StringUtils.equals(dp.getOptionValue(), MODE_LOCKED)) {
            gateway.disableDatapoint(dp, DELETE_MODE_DURATION);
        }
    }
}
