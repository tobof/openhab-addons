/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.mysensors.internal.protocol.mqtt;

import org.openhab.io.transport.mqtt.MqttService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service is created via declarative services when the MQTT transport
 * becomes available.
 * This single connection can then be reused for multiple
 * MySensors MQTT gateways.
 *
 * @author Sean McGuire
 *
 */

public class MySensorsMqttService {

    private static final Logger logger = LoggerFactory.getLogger(MySensorsMqttService.class);
    private static final MySensorsMqttService myMqttService = new MySensorsMqttService();
    private static MqttService mqttService = null;

    public MySensorsMqttService() {

    }

    public void activate(BundleContext context) {

    }

    public void deactivate(BundleContext context) {

    }

    /**
     * Called by declarative services when MQTT transport service is available
     */
    public void bind(MqttService service) {
        logger.debug("Binding MQTT Service");
        mqttService = service;
    }

    /**
     * Called by declarative services when MQTT transport service is lost
     */
    public void unbind(MqttService service) {
        mqttService = null;
        logger.debug("MQTT Service is no longer available");
    }

    public static MySensorsMqttService getInstance() {
        return myMqttService;
    }

    public MqttService getService() {
        return mqttService;
    }

}