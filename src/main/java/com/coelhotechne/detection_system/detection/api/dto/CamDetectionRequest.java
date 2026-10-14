package com.coelhotechne.detection_system.detection.api.dto;

import com.coelhotechne.detection_system.detection.application.CamDetectionCommand;
import jakarta.validation.constraints.*;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
        * Corpo de {@code POST /api/v1/cam/{camId}/detections}.
        * <pre>
 * {
 *   "event_id": "0f9e0b0e-6a43-4d6b-9a3a-7c3d2b8e1f55",
 *   "type": "person",
 *   "confidence": 0.91,
 *   "detected_at": "2026-10-09T23:41:05.120Z",
 *   "track_id": 1842,
 *   "moving": true,
 *   "taxon": null,
 *   "bbox": [412, 188, 530, 460],
 *   "snapshot_ref": "cam-7/2026/10/09/1842.jpg",
 *   "model_version": "yolo11n-2026.09"
 * }
 * </pre>
*/
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CamDetectionRequest(
        @NotNull
        UUID eventId,
        @NotBlank @Size(max = 50)
        String type,
        @DecimalMin("0.0") @DecimalMax("1.0")
        BigDecimal confidence,
        Instant detectedAt,
        Long trackId,
        Boolean moving,
        @Size(max = 100)
        String taxon,
        @Size(min = 4, max = 4)
        int[] bbox,
        @Size(max = 255)
        String snapshotRef,
        @Size(max = 64)
        String modelVersion
) {
    public CamDetectionCommand toCommand() {
        return new CamDetectionCommand(
                eventId,
                type,
                confidence,
                detectedAt,
                trackId,
                moving,
                taxon,
                bbox,
                snapshotRef,
                modelVersion
        );
    }
}