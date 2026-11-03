package com.coelhotechne.detection_system.zone.api.dto;

import com.coelhotechne.detection_system.zone.domain.enums.ZoneType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonPropertyOrder({
        "uuid",
        "name",
        "description",
        "zoneType",
        "centerLatitude",
        "centerLongitude",
        "radiusMeters",
        "active",
        "createdZoneAtDay",
        "createdBy",
        "lastModifiedBy",
        "createdAt",
        "updatedAt"
})
public record ZoneResponse(
        UUID uuid,
        String name,
        String description,
        ZoneType zoneType,
        BigDecimal centerLatitude,
        BigDecimal centerLongitude,
        Double radiusMeters,
        Boolean active,
        LocalDate createdZoneAtDay,
        String createdBy,
        String lastModifiedBy,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", shape = JsonFormat.Shape.STRING)
        LocalDateTime createdAt,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", shape = JsonFormat.Shape.STRING)
        LocalDateTime updatedAt
) {
}