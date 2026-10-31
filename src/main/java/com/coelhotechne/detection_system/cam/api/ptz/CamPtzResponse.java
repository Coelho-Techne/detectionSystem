package com.coelhotechne.detection_system.cam.api.ptz;

import com.coelhotechne.detection_system.batterysupply.domain.PowerSupply;
import com.coelhotechne.detection_system.cam.api.dto.fixed.CamConnectionSummary;
import com.coelhotechne.detection_system.cam.api.dto.fixed.CamHomologationSummary;
import com.coelhotechne.detection_system.cam.domain.base.enums.CamStatus;
import com.coelhotechne.detection_system.installation.api.dto.InstallationResponse;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.UUID;


@JsonPropertyOrder({
        "uuid",
        "version",
        "cam_model",
        "firmware_version",
        "serial_number",
        "mac_address",
        "ip_address",
        "rtsp",
        "cam_status",
        "fps",
        "bitrate",
        "compression",
        "frame_url",
        "power_supply",
        "installation",
        "zone_id",
        "cam_connection",
        "has_access_key",
        "last_communication",
        "homologation",
        "pan_left",
        "pan_right",
        "tilt_down",
        "tilt_up",
        "zoom_out",
        "zoom_in",
        "presets",
        "current_preset",
        "current_pan",
        "current_tilt",
        "current_zoom",
        "last_ptz_command",
        "last_ptz_command_at",
        "moving"
})
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CamPtzResponse(
        UUID uuid,
        Long version,
        String camModel,
        String firmwareVersion,
        String serialNumber,
        String macAddress,
        String ipAddress,
        String rtsp,
        CamStatus camStatus,
        Float fps,
        Integer bitrate,
        String compression,
        String frameUrl,
        PowerSupply powerSupply,
        InstallationResponse installation,
        UUID zoneId,
        CamConnectionSummary camConnection,
        boolean hasAccessKey,
        Instant lastCommunication,
        CamHomologationSummary homologation,
        Float panLeft,
        Float panRight,
        Float tiltDown,
        Float tiltUp,
        Float zoomOut,
        Float zoomIn,
        String presets,
        Integer currentPreset,
        Float currentPan,
        Float currentTilt,
        Float currentZoom,
        String lastPtzCommand,
        Instant lastPtzCommandAt,
        Boolean moving
) {
}
