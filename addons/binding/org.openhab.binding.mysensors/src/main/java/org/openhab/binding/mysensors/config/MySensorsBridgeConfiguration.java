/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.config;

public class MySensorsBridgeConfiguration {
    /** The serial port or ip/port the gateway is attached to */
    public String serialPort;
    public String ipAddress;
    public Integer tcpPort;
    public Integer sendDelay;
    public Integer baudRate;
    public Boolean imperial;
    public Boolean skipStartupCheck;
    public Boolean enableNetworkSanCheck;
}
