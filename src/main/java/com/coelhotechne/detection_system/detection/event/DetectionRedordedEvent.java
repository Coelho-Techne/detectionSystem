package com.coelhotechne.detection_system.detection.event;

import com.coelhotechne.detection_system.detection.domain.Detection;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;

import java.time.Instant;
import java.util.UUID;

public record DetectionRedordedEvent(
        UUID detectionId,
        DetectionDeviceType deviceType,
        UUID sourceId,
        DetectionType type,
        UUID zoneId,
        Instant detectedAt
) {
    public static DetectionRedordedEvent of(Detection detection){
        return new DetectionRedordedEvent(
                detection.getUuid(),
                detection.getDeviceType(),
                detection.getSourceId(),
                detection.getType(),
                detection.getZone() == null ? null : detection.getZone().getUuid(),
                detection.getDetectedAt());
    }
}
