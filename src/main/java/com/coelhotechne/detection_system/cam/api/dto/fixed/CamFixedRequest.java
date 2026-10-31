package com.coelhotechne.detection_system.cam.api.dto.fixed;

import com.coelhotechne.detection_system.batterysupply.domain.PowerSupply;
import com.coelhotechne.detection_system.cam.api.dto.BaseCamData;
import com.coelhotechne.detection_system.cam.domain.fixed.CamFixed;
import com.coelhotechne.detection_system.installation.api.dto.InstallationRequest;
import com.coelhotechne.detection_system.location.domain.Location;
import com.coelhotechne.detection_system.zone.domain.Zone;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CamFixedRequest(

        // ---- BaseCam ----

        @NotBlank
        String camModel,
        @NotBlank
        String firmwareVersion,
        @NotBlank
        String serialNumber,
        @NotBlank
        String macAddress,
        @NotBlank
        String ipAddress,
        String rtsp,
        Float fps,
        Integer bitrate,
        String compression,
        String frameUrl,
        UUID zoneId,
        UUID coverageLocationId,

        // ---- Alimentação / instalação / conexão ----
        // Podem ser DTOs próprios posteriormente
        @Valid
        PowerSupply powerSupply,
        @Valid
        InstallationRequest installation,

        // ---- CamFixed ----
        Float mountingAzimuthDeg,
        Float mountingElevationDeg,
        Float horizontalFovDeg,
        Float verticalFovDeg,
        Float detectionRangeMeters,
        String detectionMaskJson,
        String homographyMatrixJson,
        String baselineFrameUrl,
        String crossingLineJson,
        @DecimalMin("0.0") @DecimalMax("1.0")
        Float detectionConfidenceThreshold

) implements BaseCamData{
}
