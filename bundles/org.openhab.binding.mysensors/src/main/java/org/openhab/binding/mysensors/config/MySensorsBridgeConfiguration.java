/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mysensors.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Parameters used for bridge configuration.
 *
 * @author Tim Oberföll - Initial contribution
 *
 */
@NonNullByDefault
public class MySensorsBridgeConfiguration {
    public String serialPort = ""; // serial port the gateway is attached to
    public Boolean hardReset = false; // hard reset attached gateway with DTR
    public String ipAddress = ""; // ip address the gateway is attached to
    public Integer tcpPort = 0; // tcp port the gateway is running at
    public Integer sendDelay = 0; // delay at which messages are send from the internal queue to the MySensors network
    public Integer baudRate = 0; // baud rate used to connect the serial port
    public String brokerName = ""; // Name of the MQTT broker
    public Boolean imperial = false; // should nodes send imperial or metric values?
    public Boolean startupCheckEnabled = true; // should the startup check of the bridge at boot skipped?
    public Boolean networkSanCheckEnabled = false; // network sanity check enabled?
    public Integer networkSanCheckInterval = 0; // determines interval to start NetworkSanityCheck
    public Integer networkSanCheckConnectionFailAttempts = 0; // connection will wait this number of attempts before
    // disconnecting
    public boolean networkSanCheckSendHeartbeat = true; // network sanity checker will also send heartbeats to all known
                                                        // nodes
    public Integer networkSanCheckSendHeartbeatFailAttempts = 0; // disconnect nodes that fail to answer to heartbeat
    // request

    @Override
    public String toString() {
        return "MySensorsBridgeConfiguration [serialPort=" + serialPort + " hardReset=" + hardReset + ", ipAddress="
                + ipAddress + ", tcpPort=" + tcpPort + ", sendDelay=" + sendDelay + ", baudRate=" + baudRate
                + ", brokerName=" + brokerName + ", topicSubscribe=" + ", topicPublish=" + ", imperial=" + imperial
                + ", startupCheckEnabled=" + startupCheckEnabled + ", networSanCheckEnabled=" + networkSanCheckEnabled
                + ", networkSanCheckInterval=" + networkSanCheckInterval + ", networkSanCheckConnectionFailAttempts="
                + networkSanCheckConnectionFailAttempts + ", networkSanCheckSendHeartbeat="
                + networkSanCheckSendHeartbeat + ", networkSanCheckSendHeartbeatFailAttempts="
                + networkSanCheckSendHeartbeatFailAttempts + "]";
    }

}
