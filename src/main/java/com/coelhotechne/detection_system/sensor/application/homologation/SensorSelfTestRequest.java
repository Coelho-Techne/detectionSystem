package com.coelhotechne.detection_system.sensor.application.homologation;

import java.util.UUID;

public record SensorSelfTestRequest(
        UUID sensorId,
        String zoneName,
        String sensorName,
        UUID requestId
) {}
