package com.coelhotechne.detection_system.sensor.application.homologation;


import com.coelhotechne.detection_system.sensor.domain.homologation.SensorHomologationRecord;
import com.coelhotechne.detection_system.sensor.domain.homologation.SensorSelfTestOutcome;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public interface SensorHomologationService {
    SensorHomologationRecord getHomologation(UUID sensorId);
    SensorHomologationRecord requestSelfTest(UUID sensorId);
    boolean applySelfTestAck(UUID sensorId, UUID requestId, SensorSelfTestOutcome outcome);
    SensorHomologationRecord approve(UUID sensorId, String approvedBy);
    SensorHomologationRecord reject(UUID sensorId, String rejectedBy, String reason);
    boolean expireIfOverdue(UUID sensorId, Duration timeout, Instant now);
}
