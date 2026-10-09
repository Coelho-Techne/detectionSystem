package com.coelhotechne.detection_system.detection.application;

import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;

import java.time.Instant;
import java.util.UUID;

public record DetectionFilter(
        UUID zoneId,
        DetectionDeviceType deviceType,
        DetectionType type,
        Instant from,
        Instant to

) {
}
