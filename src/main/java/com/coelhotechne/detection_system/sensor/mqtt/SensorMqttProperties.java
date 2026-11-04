package com.coelhotechne.detection_system.sensor.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Vizualização:
 *
 * <pre>
 * sensor.mqtt.broker-url=ssl://broker.interno:8883
 * sensor.mqtt.client-id=detection-system
 * sensor.mqtt.username=detection-system
 * sensor.mqtt.password=${MQTT_PASSWORD}
 * sensor.mqtt.clean-session=false
 * sensor.mqtt.persistence-dir=/var/lib/detection-system/mqtt
 * sensor.mqtt.unique-client-id-suffix=false
 * sensor.mqtt.ingestion-threads=4
 * sensor.mqtt.ingestion-queue-capacity=5000
 * sensor.mqtt.reconnect-delay=5s
 * </pre>
 */
@ConfigurationProperties(prefix = "sensor.mqtt")
public record SensorMqttProperties (
        Boolean enabled,
        String brokerUrl,
        String clientId,
        String username,
        String password,
        Boolean cleanSession,
        String persistenceDir,
        Boolean uniqueClientIdSuffix,
        Integer ingestionThreads,
        Integer ingestionQueueCapacity,
        Duration reconnectDelay,
        Duration connectionTimeout,
        Duration keepAlive
) {
    public static final String MEMORY_PERSISTENCE = "memory";
    public SensorMqttProperties {
            enabled = enabled != null ? enabled : Boolean.TRUE;
            brokerUrl = brokerUrl != null && !brokerUrl.isBlank() ? brokerUrl : "tcp://localhost:1883";
            clientId = clientId != null  && !clientId.isBlank() ? clientId : "detection-system";
            cleanSession = cleanSession != null ? cleanSession : Boolean.FALSE;
            uniqueClientIdSuffix = uniqueClientIdSuffix != null ? uniqueClientIdSuffix : Boolean.FALSE;
            persistenceDir = persistenceDir != null ? persistenceDir : "./mqtt-persistence";
            ingestionThreads = ingestionThreads != null ? ingestionThreads : 4;
            ingestionQueueCapacity = ingestionQueueCapacity != null ? ingestionQueueCapacity : 5_000;
            reconnectDelay = reconnectDelay != null ? reconnectDelay : Duration.ofSeconds(5);
            connectionTimeout = connectionTimeout != null ? connectionTimeout : Duration.ofSeconds(10);
            keepAlive = keepAlive != null ? keepAlive : Duration.ofSeconds(60);
            if (ingestionThreads<1){
                throw new IllegalArgumentException("sensor.mqtt.ingestion-threads must be >=1");
            }
            if (ingestionQueueCapacity<1){
                throw new IllegalArgumentException("sensor.mqtt.ingestion-queue-capacity must be >=1");
            }
        }

        public boolean hasCredentials(){
        return username != null && ! username.isBlank();
        }
        public boolean usesMemoryPersistence(){
        return MEMORY_PERSISTENCE.equalsIgnoreCase(persistenceDir);
        }

        @Override
        public String toString(){
        return "SensorMqttProperties[brokerUrl=%s, clientId=%s, username=%s, cleanSession=%s, enabled=%s]"
                .formatted(brokerUrl, clientId, username, cleanSession, enabled);
    }

}
