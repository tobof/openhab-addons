/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smaenergymeter.configuration;

/**
 * The {@link EnergyMeterConfig} class holds the configuration properties of the binding.
 *
 * @author Osman Basha - Initial contribution
 */
public class EnergyMeterConfig {

    private String mcastGroup;
    private Integer port;
    private Integer pollingPeriod;

    public String getMcastGroup() {
        return mcastGroup;
    }

    public void setMcastGroup(String mcastGroup) {
        this.mcastGroup = mcastGroup;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getPollingPeriod() {
        return pollingPeriod;
    }

    public void setPollingPeriod(Integer pollingPeriod) {
        this.pollingPeriod = pollingPeriod;
    }

}
