package com.coelhotechne.detection_system.cam.api.dto.fixed;
import com.coelhotechne.detection_system.batterysupply.domain.PowerSupply;
import com.coelhotechne.detection_system.installation.api.dto.InstallationResponse;
import com.coelhotechne.detection_system.cam.domain.base.enums.CamStatus;
import com.coelhotechne.detection_system.cam.domain.base.enums.ImageQuality;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonPropertyOrder({
        "uuid",
        "createdBy",
        "lastModifiedBy",
        "createdAt",
        "updatedAt",
        "camModel",
        "firmwareVersion",
        "serialNumber",
        "macAddress",
        "ipAddress",
        "rtsp",
        "camStatus",
        "width",
        "height",
        "imageQuality",
        "fps",
        "bitrate",
        "compression",
        "frameUrl",
        "frameTimestamp",
        "timestamp",
        "powerSupply",
        "installation",
        "zoneId",
        "camConnection",
        "hasAccessKey",
        "lastCommunication",
        "homologation",
        "coverageLocationId",
        "mountingAzimuthDeg",
        "mountingElevationDeg",
        "horizontalFovDeg",
        "verticalFovDeg",
        "detectionRangeMeters",
        "detectionMaskJson",
        "homographyMatrixJson",
        "baselineFrameUrl",
        "baselineCapturedAt",
        "crossingLineJson",
        "detectionConfidenceThreshold"
})
public record CamFixedResponse(

        // ---- BaseEntity ----
        UUID uuid,
        Long version,
        String createdBy,
        String lastModifiedBy,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime updatedAt,

        // ---- BaseCam ----

        String camModel,
        String firmwareVersion,
        String serialNumber,
        String macAddress,
        String ipAddress,
        String rtsp,

        CamStatus camStatus,

        Integer width,
        Integer height,
        ImageQuality imageQuality,
        Float fps,
        Integer bitrate,
        String compression,
        String frameUrl,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant frameTimestamp,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant timestamp,
        PowerSupply powerSupply,
        InstallationResponse installation,
        UUID zoneId,
        CamConnectionSummary camConnection,
        boolean hasAccessKey,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant lastCommunication,
        CamHomologationSummary homologation,

        // ---- CamFixed ----

        UUID coverageLocationId,
        Float mountingAzimuthDeg,
        Float mountingElevationDeg,
        Float horizontalFovDeg,
        Float verticalFovDeg,
        Float detectionRangeMeters,
        String detectionMaskJson,
        String homographyMatrixJson,
        String baselineFrameUrl,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant baselineCapturedAt,
        String crossingLineJson,
        Float detectionConfidenceThreshol

) {
}