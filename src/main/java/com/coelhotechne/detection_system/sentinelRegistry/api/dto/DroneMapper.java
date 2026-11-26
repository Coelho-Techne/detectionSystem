package com.coelhotechne.detection_system.sentinelRegistry.api.dto;

import com.coelhotechne.detection_system.sentinelRegistry.domain.Drone;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component
public class DroneMapper {

    public void apply(Drone entity,DroneData data){
        Objects.requireNonNull(data,"Request cannot be null");
        entity.rename(data.droneName());
        entity.setManufacturer(trimToNull(data.manufacturer()));
        entity.setModel(trimToNull(data.model()));
        entity.setDroneType(data.droneType());
        entity.setDronePurpose(data.dronePurpose());
        entity.setControlType(data.controlType());
        entity.setWeightGrams(data.weightGrams());
        entity.setMaxPayloadGrams(data.maxPayloadGrams());
        entity.setMaxFlightTimeMinutes(data.maxFlightTimeMinutes());
        entity.setMaxSpeedKmh(data.maxSpeedKmh());
        entity.setMaxAltitudeMeters(data.maxAltitudeMeters());
        entity.setMaxRangeMeters(data.maxRangeMeters());
        entity.setFirmwareVersion(trimToNull(data.firmwareVersion()));
        entity.setCommunicationProtocol(trimToNull(data.communicationProtocol()));
        entity.setManufacturedAt(data.manufacturedAt());
        entity.setLastMaintenanceAt(data.lastMaintenanceAt());
        entity.setIpAdress(trimToNull(data.ipAddress()));
        entity.setStatus(data.status() == null || data.status());
    }

    public DroneResponse toResponse(Drone entity){
        return new DroneResponse(
                entity.getUuid(),
                entity.getVersion(),
                entity.getCreatedBy(),
                entity.getLastModifiedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDroneName(),
                entity.getSerialNumber(),
                entity.getManufacturer(),
                entity.getModel(),
                entity.getDroneType(),
                entity.getDronePurpose(),
                entity.getControlType(),
                entity.getWeightGrams(),
                entity.getMaxPayloadGrams(),
                entity.getMaxFlightTimeMinutes(),
                entity.getMaxSpeedKmh(),
                entity.getMaxAltitudeMeters(),
                entity.getMaxRangeMeters(),
                entity.getFirmwareVersion(),
                entity.getCommunicationProtocol(),
                entity.getManufacturedAt(),
                entity.getLastMaintenanceAt(),
                entity.getIpAdress(),
                entity.getStatus(),
                entity.getLastCommunication(),
                entity.getPowerSupply(),
                entity.getControllerNodeId(),
                entity.getCoupledAt()
        );
    }


    public RegisteredDrone toRegistered(Drone entity){
        return new RegisteredDrone(
                entity.getUuid(),
                entity.getDroneName(),
                entity.getSerialNumber(),
                entity.getDroneType(),
                entity.getDronePurpose(),
                entity.getControlType(),
                entity.getCommunicationProtocol(),
                entity.getFirmwareVersion(),
                entity.getMaxFlightTimeMinutes(),
                entity.getMaxSpeedKmh(),
                entity.getMaxAltitudeMeters(),
                entity.getMaxRangeMeters(),
                entity.getMaxPayloadGrams(),
                entity.isActive(),
                entity.getControllerNodeId(),
                entity.getCoupledAt()
        );
    }

    private static  String trimToNull(String value){
        if (value == null){
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
