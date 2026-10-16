package com.coelhotechne.detection_system.sensor.application.homologation;

public interface SensorSelfTestPublisher {
    void publish(SensorSelfTestRequest request);
}
