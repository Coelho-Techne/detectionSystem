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
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@Service
@Log4j2
public class MqttSensorClient implements MqttCallbackExtended{
    private static final long RETRY_SECONDS=10;
    private static final String[] EVENT_TOPICS = {
            "home/+/+/detection",
            "home/+/+/status",
            "home/+/+/telemetry",
            "home/+/+/selftest-afk"
    }; // status | telemetry | detection
    private static final int[] EVENT_QOS = {1, 1, 1, 1};
    private static final int COMMAND_QOS = 1 ;

    private final MqttClient client;
    private final SensorEventHandler eventHandler;
    private final TaskScheduler scheduler;
    private final MqttIngestionMetrics metrics;
    private final ThreadPoolExecutor ingestion;

    private volatile boolean subscriptionHealthy;
    private volatile long lastMessageAtMillis;

    public MqttSensorClient (
        MqttClient client,
        SensorEventHandler eventHandler,
        TaskScheduler scheduler,
        MqttIngestionMetrics metrics,
        SensorMqttProperties properties
    ){
        this.client = client;
        this.eventHandler = eventHandler;
        this.scheduler = scheduler;
        this.metrics = metrics;
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
        tryConnection();
    }
    public  void stop() throws InterruptedException{
        ingestion.shutdown();
        if (!ingestion.awaitTermination(10,TimeUnit.SECONDS));
        log.warn("MQTT ingestion pool did not drain in 10s; {} message(s) dropped",ingestion.getQueue());
        ingestion.shutdownNow();
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
            subscriptionHealthy = true;
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
        }catch (RuntimeException e ){
            failure = e;
            log.error("Failed to process message on {}",topic,e);
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

