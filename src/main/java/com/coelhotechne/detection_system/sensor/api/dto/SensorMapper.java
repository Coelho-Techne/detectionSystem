package com.coelhotechne.detection_system.sensor.api.dto;


import com.coelhotechne.detection_system.globalClass.mapper.GenericMapper;
import com.coelhotechne.detection_system.sensor.api.dto.homologation.SensorHomologationResponse;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.sensor.domain.homologation.SensorHomologationRecord;
import com.coelhotechne.detection_system.zone.domain.Zone;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class SensorMapper implements GenericMapper<Sensor, SensorResponse, SensorRequest> {

    @Override
    public Sensor toEntity(SensorRequest request) {
        Objects.requireNonNull(request, "Request cannot be null");

        Sensor entity = new Sensor();

        entity.setName(request.name());
        entity.setSensorNiche(request.sensorNiche());
        entity.setDataDescription(request.dataDescription());
        entity.setPowerSupply(request.powerSupply());
        entity.setInstallation(request.installation());
        return entity;
    }
    public void applyReplace(Sensor entity, SensorReplaceRequest request, Zone zone) {
        Objects.requireNonNull(request, "Request cannot be null");
        Objects.requireNonNull(zone, "Zone cannot be null");

        entity.setName(request.name());
        entity.setDataDescription(request.dataDescription());
        entity.setInstallation(request.installation());
        entity.setZone(zone);
    }

    public void applyPatch(Sensor entity, SensorPatchRequest patch, Zone resolvedZone) {
        Objects.requireNonNull(patch, "Patch cannot be null");
        if (patch.name() != null)entity.setName(patch.name());
        if (patch.dataDescription() != null)entity.setDataDescription(patch.dataDescription());
        if (patch.installation() != null)entity.setInstallation(patch.installation());
        if (patch.zoneUUID() != null)entity.setZone(resolvedZone);
    }

    public SensorHomologationResponse toHomologationResponse(SensorHomologationRecord r) {
        return new SensorHomologationResponse(
                r.getStatus(),
                r.getTestRequestedAt(),
                r.getLastTestedAt(),
                r.getTestFailure(),
                r.getTestFailureDetail(),
                r.getTestOutput(),
                r.getDecidedBy(),
                r.getDecidedAt(),
                r.getRejectionReason());
    }

    @Override
    public SensorResponse toResponse(Sensor entity) {
        Objects.requireNonNull(entity, "Entity cannot be null");

        UUID zoneUuid = entity.getZone() != null ? entity.getZone().getUuid() : null;

        return new SensorResponse(
                entity.getUuid(),
                entity.getName(),
                entity.getSensorNiche(),
                entity.getSensorStatus(),
                entity.getActivationTime(),
                entity.getMemoryUsed(),
                entity.getDataTransferValue(),
                entity.getDataDescription(),
                entity.getLastReadingAt(),
                entity.getInstallation(),
                zoneUuid,
                entity.getPowerSupply(),
                entity.getVersion(),
                entity.getCreatedBy(),
                entity.getLastModifiedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}