package com.coelhotechne.detection_system.zone.api.dto;

import com.coelhotechne.detection_system.zone.domain.Zone;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ZoneMapper  {
    public Zone toEntity(Zone entity,ZoneRequest request) {
        Objects.requireNonNull(request,"Request cannot be null");
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setZoneType(request.zoneType());
        entity.setCenterLatitude(request.centerLatitude());
        entity.setCenterLongitude(request.centerLongitude());
        entity.setRadiusMeters(request.radiusMeters());
        entity.setActive(request.active());
        return entity;
    }

    public ZoneResponse toResponse(Zone entity) {
        return new ZoneResponse(
                entity.getUuid(),
                entity.getName(),
                entity.getDescription(),
                entity.getZoneType(),
                entity.getCenterLatitude(),
                entity.getCenterLongitude(),
                entity.getRadiusMeters(),
                entity.getActive(),
                entity.getCreatedZoneAtDay(),
                entity.getCreatedBy(),
                entity.getLastModifiedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
