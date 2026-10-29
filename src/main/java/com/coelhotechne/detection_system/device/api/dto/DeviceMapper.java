package com.coelhotechne.detection_system.device.api.dto;

import com.coelhotechne.detection_system.device.domain.Device;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DeviceMapper {

    public void toEntity(Device entity,DeviceRequest request) {
        Objects.requireNonNull(request,"Request cannot be null");
        entity.setDeviceName(request.deviceName());
        entity.setDeviceType(request.deviceType());
        entity.setIpAdress(request.ipAddress());
        entity.setStatus(request.status()==null||request.status());
    }

    public DeviceResponse toResponse(Device entity) {
        return new DeviceResponse(
                entity.getUuid(),
                entity.getCreatedBy(),
                entity.getLastModifiedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeviceName(),
                entity.getDeviceType(),
                entity.getIpAdress(),
                entity.getStatus(),
                entity.getLastCommunication()
        );
    }
}
