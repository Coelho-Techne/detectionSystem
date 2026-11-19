package com.coelhotechne.detection_system.cluster.bus;

import com.coelhotechne.detection_system.cluster.config.ClusterProperties;
import com.coelhotechne.detection_system.sensor.mqtt.MqttSensorClient;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Log4j2
@Component
public class MqttClusterEventBus implements ClusterEventBus {
    private static final int QOS = 1;
    private final MqttSensorClient mqtt;
    private final ClusterProperties properties;
    private final ObjectMapper mapper;

    public MqttClusterEventBus(MqttSensorClient mqtt,ClusterProperties properties,ObjectMapper mapper){
        this.mqtt = mqtt;
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    public boolean isActive() {
        return properties.isEnabled();
    }

    @Override
    public void publish(ClusterMessage message) {
    if (!isActive()){return;}
    try {
        mqtt.publish(properties.busTopic(),mapper.writeValueAsBytes(message),QOS);
    }catch (MqttException | JacksonException ex){
        log.warn("Cluster bus: could not publish {} ({}); other nodes will not see it",message.type(),ex.getMessage());
    }
    }

    @Override
    public String nodeId() {
        return properties.nodeId();
    }
}
