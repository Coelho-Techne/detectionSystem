package com.coelhotechne.detection_system.detection.api.dto;

import com.coelhotechne.detection_system.detection.domain.BoundingBox;
import com.coelhotechne.detection_system.detection.domain.CamDetection;
import com.coelhotechne.detection_system.detection.domain.Detection;
import com.coelhotechne.detection_system.detection.domain.SensorDetection;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.zone.domain.Zone;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DetectionMapper {

    public DetectionResponse toResponse(Detection entity) {
        Objects.requireNonNull(entity,"Detection cannot be null");
        Zone zone = entity.getZone();
        return new DetectionResponse(
                entity.getUuid(),
                entity.getSourceEventId(),
                entity.getDeviceType(),
                entity.getSourceId(),
                entity.getType(),
                entity.getUnmappedType(),
                zone == null ? null : zone.getUuid(),
                zone == null ? null : zone.getName(),
                entity.getDetectedAt(),
                entity.getReceivedAt(),
                entity.getConfidence(),
                entity.getDescription(),
                entity.isSourceHomologated(),
                entity.getGasType()
        );
    }

    public DetectionDetailResponse toDetail(Detection entity){
        DetectionResponse sumary = toResponse(entity);
        if (entity instanceof SensorDetection sd){
            Sensor sensor = sd.getSensor();
            return new DetectionDetailResponse(sumary,
                    new DetectionDetailResponse.SensorBlock(
                            sensor.getUuid(),
                            sensor.getName(),
                            sensor.getSensorNiche(),
                            sd.getMeasuredValue(),
                            sd.getMeasuredUnit(),
                            sd.getGasType() == null ? null : sd.getGasType()), null);
        }

        if (entity instanceof CamDetection cd){
            BoundingBox bbox=cd.getBbox();
            return new DetectionDetailResponse(
                    sumary,
            null,
                    new DetectionDetailResponse.CameraBlock(
                            cd.getCam().getUuid(),
                            cd.getCam().getCamModel(),
                            cd.getTrackId(),
                            cd.getMoving(),
                            cd.getTaxon(),
                            bbox == null ? null : bbox.toArray(),
                            cd.getSnapshotRef(),
                            cd.getModelVersion()
            ));
        }
        return new DetectionDetailResponse(sumary,null,null);
    }

}
