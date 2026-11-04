package com.coelhotechne.detection_system.sensor.api.dto;


import com.coelhotechne.detection_system.batterysupply.domain.PowerSupply;
import com.coelhotechne.detection_system.installation.api.dto.InstallationResponse;
import com.coelhotechne.detection_system.installation.domain.Installation;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorNiche;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonPropertyOrder({
        "uuid",
        "name",
        "sensorNiche",
        "status",
        "activationTime",
        "memoryUsed",
        "dataTransferValue",
        "dataDescription",
        "lastReadingAt",
        "installation",
        "zoneUuid",
        "powerSupply",
        "version",
        "createdBy",
        "lastModifiedBy",
        "createdAt",
        "updatedAt"
})
public record SensorResponse(
        UUID uuid,
        String name,
        SensorNiche sensorNiche,
        SensorStatus status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime activationTime,
        BigDecimal memoryUsed,
        BigDecimal dataTransferValue,
        String dataDescription,
        Instant lastReadingAt,
        InstallationResponse installation,
        UUID zoneUuid,
        PowerSupply powerSupply,
        Long version,
        String createdBy,
        String lastModifiedBy,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {
}