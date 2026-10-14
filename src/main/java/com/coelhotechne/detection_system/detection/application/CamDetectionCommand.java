package com.coelhotechne.detection_system.detection.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CamDetectionCommand(
        UUID eventId,
        String type,
        BigDecimal confidence,
        Instant detectedAt,
        Long trackId,
        Boolean moving,
        String taxon,
        int[] bbox,
        String snapshotRef,
        String modelVersion
) {
}
