package com.coelhotechne.detection_system.sensor.mqtt;

import com.coelhotechne.detection_system.sensor.exceptions.SensorConcurrentModificationException;
import com.coelhotechne.detection_system.sensor.exceptions.SensorEventParseException;
import com.coelhotechne.detection_system.sensor.exceptions.SensorNotFoundException;
import com.coelhotechne.detection_system.sensor.exceptions.UnsupportedSensorEventException;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.Timer;


import java.util.concurrent.BlockingQueue;

@Component
public class MqttIngestionMetrics {

    private final MeterRegistry registry;

    public MqttIngestionMetrics (MeterRegistry registry){
        this.registry = registry;
    }

    public  void bindQueue(BlockingQueue<?> queue){
        registry.gauge("detection.mqtt,queue.size", queue, BlockingQueue::size);
    }

    public Timer.Sample start(){
        return Timer.start(registry);
    }

    public void record(
            Timer.Sample sample,
            String topicType,
            Throwable failure
            ){
        sample.stop(registry.timer("detection.mqtt.processing","topic_type",topicType));
        registry.counter("detection.mqtt.messages","topic_type",topicType,"result",result(failure));
    }

    private String result(Throwable failure) {
        if (failure == null)return "ok";
        if (failure instanceof SensorEventParseException)return "parse_error";
        if (failure instanceof SensorNotFoundException)return "unknown_sensor";
        if (failure instanceof UnsupportedSensorEventException)return "unsupported";
        if (failure instanceof OptimisticLockingFailureException ||
                failure instanceof SensorConcurrentModificationException)return "conflict";
        return "error";
    }

    static String topicType(String topic){
        int topicIndex = topic.indexOf('/');
        return topicIndex < 0 ? "unknown" : topic.substring(topicIndex+1);
    }
}
