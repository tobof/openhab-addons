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

    /**
     * Is a serial or ip gateway?
     */
    private MySensorsGatewayType gatewayType;

    // GLOBALS
    /**
     * Delay at which messages are send from the internal queue to the MySensors network
     */
    private Integer sendDelay;

    /**
     * Should nodes send imperial or metric values?
     */
    private Boolean imperial; //

    /**
     * Should the startup check of the bridge at boot skipped?
     */
    private Boolean skipStartupCheck;

    /**
     * Network sanity check enabled?
     */
    private Boolean enableNetworkSanCheck;

    /**
     * Determines interval to start NetworkSanityCheck
     */
    private Integer sanityCheckerInterval;

    /**
     * Connection will wait this number of attempts before disconnecting
     */
    private Integer sanCheckConnectionFailAttempts;

    /**
     * Network sanity checker will also send heartbeats to all known nodes
     */
    private boolean sanCheckSendHeartbeat;

    /**
     * Disconnect nodes that fail to answer to heartbeat request
     */
    private Integer sanCheckSendHeartbeatFailAttempts;

    // SERIAL
    /**
     * Serial port the gateway is attached to
     */
    private String serialPort;

    /**
     * Baud rate used to connect the serial port
     */
    private Integer baudRate;

    // Ip
    /**
     * ip address the gateway is attached to
     */
    private String ipAddress;

    /**
     * tcp port the gateway is running at
     */
    private Integer tcpPort;

    /**
     * URL of MQTT broker
     */
    private String url;

    /**
     * MQQT broker username
     */
    private String username;

    /**
     * MQTT broker password
     */
    private String password;

    /**
     * MQTT topic to subscribe to
     */
    private String topicSubscribe;

    /**
     * MQTT topic to publish to
     */
    private String topicPublish;

    /**
     * MQTT client ID
     */
    private String clientid;

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

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTopicSubscribe() {
        return topicSubscribe;
    }

    public void setTopicSubscribe(String topicSubscribe) {
        this.topicSubscribe = topicSubscribe;
    }

    public String getTopicPublish() {
        return topicPublish;
    }

    public void setTopicPublish(String topicPublish) {
        this.topicPublish = topicPublish;
    }

    public String getClientId() {
        return clientid;
    }

    public void setClientId(String clientid) {
        this.clientid = clientid;
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

    public Integer getSanityCheckerInterval() {
        return sanityCheckerInterval;
    }

    public void setSanityCheckerInterval(Integer sanityCheckerInterval) {
        this.sanityCheckerInterval = sanityCheckerInterval;
    }

    public Integer getSanCheckConnectionFailAttempts() {
        return sanCheckConnectionFailAttempts;
    }

    public void setSanCheckConnectionFailAttempts(Integer sanCheckConnectionFailAttempts) {
        this.sanCheckConnectionFailAttempts = sanCheckConnectionFailAttempts;
    }

    public boolean getSanCheckSendHeartbeat() {
        return sanCheckSendHeartbeat;
    }

    public void setSanCheckSendHeartbeat(boolean sanCheckSendHeartbeat) {
        this.sanCheckSendHeartbeat = sanCheckSendHeartbeat;
    }

    public Integer getSanCheckSendHeartbeatFailAttempts() {
        return sanCheckSendHeartbeatFailAttempts;
    }

    public void setSanCheckSendHeartbeatFailAttempts(Integer sanCheckSendHeartbeatFailAttempts) {
        this.sanCheckSendHeartbeatFailAttempts = sanCheckSendHeartbeatFailAttempts;
    }

    @Override
    public String toString() {
        return "MySensorsGatewayConfig [gatewayType=" + gatewayType + ", sendDelay=" + sendDelay + ", imperial="
                + imperial + ", skipStartupCheck=" + skipStartupCheck + ", enableNetworkSanCheck="
                + enableNetworkSanCheck + ", sanityCheckerInterval=" + sanityCheckerInterval
                + ", sanCheckConnectionFailAttempts=" + sanCheckConnectionFailAttempts + ", sanCheckSendHeartbeat="
                + sanCheckSendHeartbeat + ", sanCheckSendHeartbeatFailAttempts=" + sanCheckSendHeartbeatFailAttempts
                + ", serialPort=" + serialPort + ", baudRate=" + baudRate + ", ipAddress=" + ipAddress + ", tcpPort="
                + tcpPort + "]";
    }
}
