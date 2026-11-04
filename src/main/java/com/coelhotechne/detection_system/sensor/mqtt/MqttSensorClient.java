package com.coelhotechne.detection_system.sensor.mqtt;


import com.coelhotechne.detection_system.sensor.application.event.SensorEventHandler;
import io.micrometer.core.instrument.Timer;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@Service
@Log4j2
public class MqttSensorClient implements MqttCallbackExtended{
    private static final int COMMAND_QOS = 1 ;
    private static final String[] EVENT_TOPICS = {
            "home/+/+/detection",
            "home/+/+/status",
            "home/+/+/telemetry",
            "home/+/+/selftest-afk"
    }; // status | telemetry | detection
    private static final int[] EVENT_QOS = {1, 1, 1, 1};

    private final MqttClient client;
    private final MqttConnectOptions options;
    private final SensorEventHandler eventHandler;
    private final TaskScheduler scheduler;
    private final MqttIngestionMetrics metrics;
    private final ThreadPoolExecutor ingestion;
    private final boolean enabled;
    private final Duration retryDelay;

    private volatile boolean subscriptionHealthy;
    private volatile long lastMessageAtMillis;
    private volatile boolean stopping;

    public MqttSensorClient (
        MqttClient client,
        MqttConnectOptions options,
        SensorEventHandler eventHandler,
        TaskScheduler scheduler,
        MqttIngestionMetrics metrics,
        SensorMqttProperties properties
    ){
        this.client = client;
        this.options = options;
        this.eventHandler = eventHandler;
        this.scheduler = scheduler;
        this.metrics = metrics;
        this.enabled = properties.enabled();
        this.retryDelay=properties.reconnectDelay();
        AtomicInteger counter = new AtomicInteger();
        this.ingestion = new ThreadPoolExecutor(
                properties.ingestionThreads(),
                properties.ingestionThreads(),
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(properties.ingestionQueueCapacity()),
                r -> {
                    Thread t = new Thread(r,"mqtt-ingest"+counter.incrementAndGet());
                    t.setDaemon(true);
                    return  t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy());
            metrics.bindQueue(ingestion.getQueue());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start(){
        client.setCallback(this);
        if (!enabled){
            log.warn("MQTT disabled (sensor.mqtt.enabled=false): no sensor events will be received");
            return;
        }
        tryConnection();
    }
    public  void stop() throws InterruptedException{
        stopping =true;
        try {
            if (client.isConnected()){
                client.disconnect(5_000);
            }
        }catch (MqttException ex){
            log.warn("MQTT disconnect failed: ",ex);
        }
        ingestion.shutdown();
        if (!ingestion.awaitTermination(10,TimeUnit.SECONDS)){
            log.warn("MQTT ingestion pool did not drain in 10s; {} message(s) dropped",ingestion.getQueue());
            ingestion.shutdownNow();
        }
        try {
            client.close();
        }catch (MqttException ex){
            log.debug("MQTT client close failed",ex);
        }
    }

    private void tryConnection() {
        if (stopping){
            return;
        }
    try {
        client.connect(options);
    }catch (MqttException ex){
        log.warn("MQTT broker unavailable ({}) retrying in {}s",ex.getMessage(),retryDelay.toSeconds());
        scheduler.schedule(this::tryConnection, Instant.now().plus(retryDelay));
        }
    }
    private void subscribe(){
        if (stopping){
            return;
        }
        try {
            client.subscribe(EVENT_TOPICS,EVENT_QOS);
            subscriptionHealthy = true;
            log.info("Subscribed to {}",String.join(", ",EVENT_TOPICS));
        }catch (MqttException ex){
            subscriptionHealthy=true;
            log.error("Failed to subscribe; retrying in {}s; error: {}",retryDelay.toSeconds(),ex);
            scheduler.schedule(this::subscribe,Instant.now().plus(retryDelay));
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
        subscriptionHealthy = false;
    log.warn("MQTT connection lost; automatic reconnect in progress",throwable);
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
    lastMessageAtMillis = System.currentTimeMillis();
    String payload = new String(mqttMessage.getPayload(),StandardCharsets.UTF_8);
    ingestion.execute(()-> process(topic,payload));
    }

    private void process(String topic, String payload){
        Timer.Sample sample = metrics.start();
        Throwable failure = null;
        try {
            eventHandler.handle(topic,payload);
        }catch (RuntimeException ex){
            failure = ex;
            log.error("Failed to process message on {}",topic,ex);
        }finally {
            metrics.record(sample,MqttIngestionMetrics.topicType(topic),failure);
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
    public void publishCommand(String topic, String command)throws MqttException{
        publish(topic,command.getBytes(StandardCharsets.UTF_8),COMMAND_QOS);
    }
    public boolean isSubscriptionHealthy(){
        return subscriptionHealthy;
    }

    public long secondsSinceLastMessage(){
        return lastMessageAtMillis  == 0 ? -1 :(System.currentTimeMillis() - lastMessageAtMillis)/1000;
    }
}

