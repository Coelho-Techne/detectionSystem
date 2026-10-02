package com.coelhotechne.detection_system.sensor.api.dto.homologation;

import com.coelhotechne.detection_system.sensor.domain.homologation.enums.SensorHomologationStatus;
import com.coelhotechne.detection_system.sensor.domain.homologation.enums.SensorSelfTestFailure;

import java.time.Instant;

// para projeção de leitura
public record SensorHomologationSummary(
        SensorHomologationStatus status,
        Instant testRequestedAt,
        SensorSelfTestFailure testFailure,
        String testFailureDetail,
        String decidedBy,
        Instant decidedAt,
        String decisionReason
) {
}
