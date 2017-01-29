/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.gateway;

public class MySensorsGatewayConfig {

    private MySensorsGatewayType gatewayType; // is a serial or ip gateway?

    // Globals
    private Integer sendDelay; // delay at which messages are send from the internal queue to the MySensors network
    private Boolean imperial; // should nodes send imperial or metric values?
    private Boolean skipStartupCheck; // should the startup check of the bridge at boot skipped?
    private Boolean enableNetworkSanCheck; // network sanity check enabled?

    // Serial
    private String serialPort; // serial port the gateway is attached to
    private Integer baudRate; // baud rate used to connect the serial port

    // Ip
    private String ipAddress; // ip address the gateway is attached to
    private Integer tcpPort; // tcp port the gateway is running at

    public MySensorsGatewayType getGatewayType() {
        return gatewayType;
    }

    public void setGatewayType(MySensorsGatewayType gatewayType) {
        this.gatewayType = gatewayType;
    }

    public String getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(String serialPort) {
        this.serialPort = serialPort;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(Integer tcpPort) {
        this.tcpPort = tcpPort;
    }

    public Integer getSendDelay() {
        return sendDelay;
    }

    public void setSendDelay(Integer sendDelay) {
        this.sendDelay = sendDelay;
    }

    public Integer getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(Integer baudRate) {
        this.baudRate = baudRate;
    }

    public Boolean getImperial() {
        return imperial;
    }

    public void setImperial(Boolean imperial) {
        this.imperial = imperial;
    }

    public Boolean getSkipStartupCheck() {
        return skipStartupCheck;
    }

    public void setSkipStartupCheck(Boolean skipStartupCheck) {
        this.skipStartupCheck = skipStartupCheck;
    }

    public Boolean getEnableNetworkSanCheck() {
        return enableNetworkSanCheck;
    }

    public void setEnableNetworkSanCheck(Boolean enableNetworkSanCheck) {
        this.enableNetworkSanCheck = enableNetworkSanCheck;
    }

}
