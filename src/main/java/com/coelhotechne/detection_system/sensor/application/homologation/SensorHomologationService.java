package com.coelhotechne.detection_system.sensor.application.homologation;


import com.coelhotechne.detection_system.sensor.domain.homologation.SensorHomologationRecord;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public interface SensorHomologationService {
    SensorHomologationRecord getHomologation(UUID sensorId);
    SensorHomologationRecord requestSelfTest(UUID sensorId);
    SensorHomologationRecord cancelSelfTest(UUID sensorId,String cancelledBy);
    SensorHomologationRecord approve(UUID sensorId, String approvedBy);
    SensorHomologationRecord reject(UUID sensorId, String rejectedBy, String reason);
    SensorHomologationRecord reopen(UUID sensorId, String reopenedBy);
    boolean expireIfOverdue(UUID sensorId, Duration timeout, Instant now);
}
