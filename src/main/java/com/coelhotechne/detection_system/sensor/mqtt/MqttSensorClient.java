package com.coelhotechne.detection_system.sensor.mqtt;


import com.coelhotechne.detection_system.sensor.application.event.SensorEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;


@Service
@Log4j2
@RequiredArgsConstructor
public class MqttSensorClient implements MqttCallbackExtended{
    private static final long RETRY_SECONDS=10;
    private static final String[] EVENT_TOPICS = {
            "home/+/+/detection",
            "home/+/+/status",
            "home/+/+/telemetry",
            "home/+/+/selftest-afk"
    }; // status | telemetry | detection
    private static final int[] EVENT_QOS = {1, 1, 1, 1};

    private final MqttClient client;
    private final SensorEventHandler eventHandler;
    private final TaskScheduler scheduler;

    @EventListener(ApplicationReadyEvent.class)
    public void start(){
        client.setCallback(this);
        tryConnection();
    }

    private void tryConnection() {
    try {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        options.setConnectionTimeout(10);
        client.connect(options);
    }catch (MqttException e){
        log.warn("MQTT broker unavailable retrying in {}s",RETRY_SECONDS,e);
        scheduler.schedule(this::tryConnection, Instant.now().plusSeconds(RETRY_SECONDS));
    }
    }
    private void subscribe(){
        try {
            client.subscribe(EVENT_TOPICS,EVENT_QOS);
            log.info("Subscribed to {}",String.join(", ",EVENT_TOPICS));
        }catch (MqttException e){
            log.error("Failed to subscribe; retrying in {}s",RETRY_SECONDS,e);
            scheduler.schedule(this::subscribe,Instant.now().plusSeconds(RETRY_SECONDS));
        }
    }
    // :::::::::::::::::::::::::: MQTT Extend Patch ::::::::::::::::::::::::::
    @Override
    public void connectComplete(boolean reconnect, String serverUri) {
    log.info("MQTT {} to {}",reconnect ? "reconnected" : "connected",serverUri);
    scheduler.schedule(this::subscribe,Instant.now());
    }

    @Override
    public void connectionLost(Throwable throwable) {
    log.warn("MQTT connection lost; automatic reconnect in progress",throwable);
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
    try {
        eventHandler.handle(s,new String(mqttMessage.getPayload(), StandardCharsets.UTF_8));
    }catch (RuntimeException e){
        log.error("Failed to process message on {}",s,e);
    }
    }
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        // :::::::::::::::::::::::::: Nothing ::::::::::::::::::::::::::
    }
    public boolean isConnected(){
        return client.isConnected();
    }

    public void publish(String topic, byte[] payload, int qos)throws MqttException{
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        message.setRetained(false);
        client.publish(topic,message);
    }
}

