package com.coelhotechne.detection_system.sensor.api.dto.homologation;

import com.coelhotechne.detection_system.sensor.domain.homologation.enums.SensorHomologationStatus;
import com.coelhotechne.detection_system.sensor.domain.homologation.enums.SensorSelfTestFailure;

import java.time.Instant;

public record SensorHomologationResponse(
        SensorHomologationStatus status,
        Instant testRequestedAt,
        Instant lastTestedAt,
        SensorSelfTestFailure testFailure,
        String testFailureDetail,
        String testOutput,
        String decidedBy,
        Instant decidedAt,
        String rejectionReason
) {
}
