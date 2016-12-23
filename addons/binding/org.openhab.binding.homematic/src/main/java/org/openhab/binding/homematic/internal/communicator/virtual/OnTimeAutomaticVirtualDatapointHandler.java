/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import java.io.IOException;

import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.misc.MiscUtils;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointConfig;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A virtual Number datapoint which adds a automatic ON_TIME datapoint on supported device. This datapoint sets the
 * ON_TIME datapoint every time a STATE or LEVEL datapoint is set, so that the light turns off automatically by the
 * device after the specified time.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class OnTimeAutomaticVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    private static final Logger logger = LoggerFactory.getLogger(OnTimeAutomaticVirtualDatapointHandler.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(HmDevice device) {
        for (HmChannel channel : device.getChannels()) {
            HmDatapointInfo dpInfoOnTime = HmDatapointInfo.createValuesInfo(channel, DATAPOINT_NAME_ON_TIME);
            if (channel.hasDatapoint(dpInfoOnTime)) {
                HmDatapointInfo dpInfoLevel = HmDatapointInfo.createValuesInfo(channel, DATAPOINT_NAME_LEVEL);
                HmDatapointInfo dpInfoState = HmDatapointInfo.createValuesInfo(channel, DATAPOINT_NAME_STATE);
                if (channel.hasDatapoint(dpInfoLevel) || channel.hasDatapoint(dpInfoState)) {
                    HmDatapoint dpOnTime = channel.getDatapoint(dpInfoOnTime);
                    HmDatapoint dpOnTimeAutomatic = dpOnTime.clone();
                    dpOnTimeAutomatic.setName(VIRTUAL_DATAPOINT_NAME_ON_TIME_AUTOMATIC);
                    dpOnTimeAutomatic.setDescription(VIRTUAL_DATAPOINT_NAME_ON_TIME_AUTOMATIC);
                    addDatapoint(channel, dpOnTimeAutomatic);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandle(HmDatapoint dp, Object value) {
        boolean isLevel = DATAPOINT_NAME_LEVEL.equals(dp.getName()) && value != null && value instanceof Number
                && ((Number) value).doubleValue() > 0.0;
        boolean isState = DATAPOINT_NAME_STATE.equals(dp.getName()) && MiscUtils.isTrueValue(value);

        return ((isLevel || isState) && getVirtualDatapointValue(dp.getChannel()) > 0.0)
                || VIRTUAL_DATAPOINT_NAME_ON_TIME_AUTOMATIC.equals(dp.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(VirtualGateway gateway, HmDatapoint dp, HmDatapointConfig dpConfig, Object value)
            throws IOException, HomematicClientException {
        if (!VIRTUAL_DATAPOINT_NAME_ON_TIME_AUTOMATIC.equals(dp.getName())) {
            HmChannel channel = dp.getChannel();
            HmDatapoint dpOnTime = channel
                    .getDatapoint(HmDatapointInfo.createValuesInfo(channel, DATAPOINT_NAME_ON_TIME));
            if (dpOnTime != null) {
                gateway.sendDatapoint(dpOnTime, new HmDatapointConfig(true), getVirtualDatapointValue(channel));
            } else {
                logger.warn(
                        "Can't find ON_TIME datapoint in channel '{}' in device '{}', ignoring virtual datapoint '{}'",
                        channel.getNumber(), channel.getDevice().getAddress(),
                        VIRTUAL_DATAPOINT_NAME_ON_TIME_AUTOMATIC);
            }
            gateway.sendDatapointIgnoreVirtual(dp, dpConfig, value);
        } else {
            dp.setValue(value);
        }
    }

    /**
     * Returns the virtual datapoint value or 0 if not specified.
     */
    private Double getVirtualDatapointValue(HmChannel channel) {
        HmDatapoint dpOnTimeAutomatic = channel
                .getDatapoint(HmDatapointInfo.createValuesInfo(channel, VIRTUAL_DATAPOINT_NAME_ON_TIME_AUTOMATIC));
        return dpOnTimeAutomatic == null || dpOnTimeAutomatic.getValue() == null
                || dpOnTimeAutomatic.getType() != HmValueType.FLOAT ? 0.0
                        : ((Number) dpOnTimeAutomatic.getValue()).doubleValue();
    }
}
