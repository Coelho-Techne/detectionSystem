package com.coelhotechne.detection_system.sensor.application.homologation;


import com.coelhotechne.detection_system.sensor.domain.homologation.SensorHomologationRecord;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public interface SensorHomologationService {
    SensorHomologationRecord runConnectionTest(UUID camId);
    SensorHomologationRecord approve(UUID camId, String approvedBy);
    SensorHomologationRecord reject(UUID camId, String rejectedBy, String reason);
    boolean expireIfOverdue(UUID sensorId, Duration timeout, Instant now);
}
