package com.coelhotechne.detection_system.cam.api.ptz;

import com.coelhotechne.detection_system.batterysupply.domain.PowerSupply;
import com.coelhotechne.detection_system.cam.api.dto.BaseCamData;
import com.coelhotechne.detection_system.installation.api.dto.InstallationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CamPtzRequest (
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
        @Valid
        PowerSupply powerSupply,
        @Valid
        InstallationRequest installation,
        Float panLeft,
        Float panRight,
        Float tiltDown,
        Float tiltUp,
        Float zoomOut,
        Float zoomIn,
        @Size(max = 10_000)
        String presets
)implements BaseCamData {
}
