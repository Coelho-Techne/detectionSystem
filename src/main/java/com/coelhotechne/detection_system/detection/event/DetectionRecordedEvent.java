package com.coelhotechne.detection_system.detection.event;

import com.coelhotechne.detection_system.detection.domain.CamDetection;
import com.coelhotechne.detection_system.detection.domain.Detection;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;

import java.time.Instant;
import java.util.UUID;

public record DetectionRecordedEvent(
        UUID detectionId,
        DetectionDeviceType deviceType,
        UUID sourceId,
        DetectionType type,
        UUID zoneId,
        Instant detectedAt,
        Long trackId,
        boolean sourceHomologated
) {
    public static DetectionRecordedEvent of(Detection detection){
        return new DetectionRecordedEvent(
                detection.getUuid(),
                detection.getDeviceType(),
                detection.getSourceId(),
                detection.getType(),
                detection.getZone() == null ? null : detection.getZone().getUuid(),
                detection.getDetectedAt(),
                detection instanceof CamDetection cam ? cam.getTrackId() : null,
                detection.isSourceHomologated());
    }
}
