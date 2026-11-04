package com.coelhotechne.detection_system.sensor.mqtt;

import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Log4j2
@Configuration
public class MqttClientConfig {

    @Bean(destroyMethod = "")
    public MqttClient mqttClient(SensorMqttProperties properties)throws Exception{
        String clientId = properties.uniqueClientIdSuffix() ?
                properties.clientId() + "-" + UUID.randomUUID().toString().substring(0,8)
                : properties.clientId();
        MqttClientPersistence persistence = properties.usesMemoryPersistence() ?
                new MemoryPersistence() :
                new MqttDefaultFilePersistence(properties.persistenceDir());
        log.info("MQTT client {} for {}({} persistence)",clientId,properties.brokerUrl(),
                properties.usesMemoryPersistence() ? "memory":properties.persistenceDir());
        return new MqttClient(properties.brokerUrl(),clientId,persistence);
    }
    @Bean
    public MqttConnectOptions mqttConnectOptions(SensorMqttProperties properties){
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(properties.cleanSession());
        options.setConnectionTimeout((int) properties.connectionTimeout().toSeconds());
        options.setKeepAliveInterval((int) properties.keepAlive().toSeconds());
        options.setMaxReconnectDelay((int) Math.max(1_000,properties.reconnectDelay().toMillis()));
        if (properties.hasCredentials()){
            options.setUserName(properties.username());
            options.setPassword(properties.password() == null ? new char[0]:properties.password().toCharArray());
        }
        return options;
    }
}
