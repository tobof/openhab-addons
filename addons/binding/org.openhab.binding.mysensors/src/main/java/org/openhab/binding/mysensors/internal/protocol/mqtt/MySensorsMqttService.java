package org.openhab.binding.mysensors.internal.protocol.mqtt;

import org.openhab.io.transport.mqtt.MqttService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySensorsMqttService {

    private static final Logger logger = LoggerFactory.getLogger(MySensorsMqttService.class);
    private static final MySensorsMqttService myMqttService = new MySensorsMqttService();
    private static MqttService mqttService;

    public MySensorsMqttService() {

    }

    public void activate(BundleContext context) {

    }

    public void deactivate(BundleContext context) {

    }

    public void bind(MqttService service) {
        logger.debug("Binding MQTT Service");
        mqttService = service;
    }

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
