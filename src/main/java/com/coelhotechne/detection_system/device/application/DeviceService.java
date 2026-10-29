package com.coelhotechne.detection_system.device.application;

import com.coelhotechne.detection_system.device.api.dto.DeviceCreatedResponse;
import com.coelhotechne.detection_system.device.api.dto.DeviceRequest;
import com.coelhotechne.detection_system.device.api.dto.DeviceResponse;
import com.coelhotechne.detection_system.device.domain.Device;

import java.util.List;
import java.util.UUID;

public interface DeviceService {
    Device requireDevice(UUID deviceId);
    List<DeviceResponse> findDeviceList();
    DeviceResponse findDeviceId(UUID uuid);
    DeviceCreatedResponse createDevice(DeviceRequest deviceRequest);
    DeviceResponse updateDevice(UUID uuid,DeviceRequest deviceRequest);
    void deleteDevice(UUID uuid);
}
