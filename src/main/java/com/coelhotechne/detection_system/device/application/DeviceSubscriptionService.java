package com.coelhotechne.detection_system.device.application;

import com.coelhotechne.detection_system.device.api.dto.DeviceSubscriptionRequest;
import com.coelhotechne.detection_system.device.api.dto.DeviceSubscriptionResponse;
import com.coelhotechne.detection_system.device.domain.Device;
import com.coelhotechne.detection_system.device.domain.DeviceAlertSubscription;
import com.coelhotechne.detection_system.device.exceptions.DeviceNotFoundException;
import com.coelhotechne.detection_system.device.exceptions.DeviceSubscriptionNotFoundException;
import com.coelhotechne.detection_system.device.infrastructure.DeviceAlertSubscriptionRepository;
import com.coelhotechne.detection_system.zone.application.ZoneService;
import com.coelhotechne.detection_system.zone.domain.Zone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceSubscriptionService {

    private final DeviceService deviceService;
    private final ZoneService zoneService;
    private final DeviceAlertSubscriptionRepository repository;

    @Transactional(readOnly = true)
    public List<DeviceSubscriptionResponse>list(UUID deviceId){
        deviceService.requireDevice(deviceId);
        return repository.findByDeviceUuid(deviceId).stream().map(DeviceSubscriptionResponse::from).toList();
    }

    @Transactional
    public DeviceSubscriptionResponse upsert(UUID deviceId,DeviceSubscriptionRequest request){
        Device device = deviceService.requireDevice(deviceId);
        Zone zone = request.zoneId() == null ? null : zoneService.requireZone(request.zoneId());
        Optional<DeviceAlertSubscription>existing = zone == null ?
                repository.findByDeviceUuidAndZoneIsNull(deviceId):
                repository.findByDeviceUuidAndZoneUuid(deviceId,zone.getUuid());
        DeviceAlertSubscription subscription = existing.map(s -> {
            s.changeMinSeverity(request.minSeverity());
            s.isActive();
            return s;
        }).orElseGet(() -> repository.save(new DeviceAlertSubscription(device,
                zone, request.minSeverity())));
        return DeviceSubscriptionResponse.from(subscription);
    }
    @Transactional
    public void delete(UUID deviceId,UUID subscriptionId){
        DeviceAlertSubscription subscription = repository.findByUuidAndDeviceUuid(subscriptionId,deviceId)
                .orElseThrow(()->new DeviceSubscriptionNotFoundException(deviceId,subscriptionId));
        repository.delete(subscription);
    }


}
