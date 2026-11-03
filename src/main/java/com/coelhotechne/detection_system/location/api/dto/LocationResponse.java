package com.coelhotechne.detection_system.location.api.dto;


import com.coelhotechne.detection_system.location.domain.Location;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonPropertyOrder({
        "uuid",
        "name",
        "latitude",
        "longitude",
        "zoneUuid",
        "createdBy",
        "lastModifiedBy",
        "createdAt",
        "updatedAt"
})
public record LocationResponse(
        UUID uuid,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        UUID zoneUuid,
        String createdBy,
        String lastModifiedBy,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", shape = JsonFormat.Shape.STRING)
        LocalDateTime createdAt,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", shape = JsonFormat.Shape.STRING)
        LocalDateTime updatedAt
) {
}
