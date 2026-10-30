package com.coelhotechne.detection_system.device.application;

import com.coelhotechne.detection_system.device.api.dto.DeviceCreatedResponse;
import com.coelhotechne.detection_system.device.api.dto.DeviceMapper;
import com.coelhotechne.detection_system.device.api.dto.DeviceRequest;
import com.coelhotechne.detection_system.device.api.dto.DeviceResponse;
import com.coelhotechne.detection_system.device.api.dto.auth.DeviceKeyGenerator;
import com.coelhotechne.detection_system.device.domain.Device;
import com.coelhotechne.detection_system.device.exceptions.DeviceNotFoundException;
import com.coelhotechne.detection_system.device.infrastructure.DeviceAlertSubscriptionRepository;
import com.coelhotechne.detection_system.device.infrastructure.DeviceRepository;

import com.coelhotechne.detection_system.globalClass.security.AccessKeyHasher;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@Service
@Log4j2
public class DeviceServiceImp implements DeviceService{
    private final DeviceRepository repository;
    private final DeviceAlertSubscriptionRepository subscriptionRepository;
    private final DeviceMapper mapper;
    private final DeviceKeyGenerator keyGenerator;
    private final AccessKeyHasher accessKeyHasher;
    private final Clock clock;

    @Override
    public Device requireDevice(UUID deviceId) {
        Objects.requireNonNull(deviceId,"Device cannot be null");
        return repository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException(deviceId.toString(),null,"Device not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> findDeviceList() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceResponse findDeviceId(UUID uuid) {
        return mapper.toResponse(requireDevice(uuid));
    }

    @Override
    @Transactional
    public DeviceCreatedResponse createDevice(DeviceRequest deviceRequest) {
        Device entity = new Device();
        mapper.toEntity(entity,deviceRequest);
        String rawKey = keyGenerator.generate();
        entity.setAccessKey(accessKeyHasher.hash(rawKey));
        entity.setLastCommunication(LocalDateTime.now(clock));
        Device create = repository.save(entity);
        return new DeviceCreatedResponse(mapper.toResponse(create),rawKey);
    }

    @Override
    @Transactional
    public DeviceResponse updateDevice(UUID uuid, DeviceRequest deviceRequest) {

        Device update=requireDevice(uuid);
        mapper.toEntity(update,deviceRequest);
        return mapper.toResponse(repository.saveAndFlush(update));
    }

    @Override
    @Transactional
    public void deleteDevice(UUID uuid) {
        Device deleteDevice=requireDevice(uuid);
        subscriptionRepository.deleteByDeviceUuid(uuid);
        repository.delete(deleteDevice);
        log.info("Device {} deleted",uuid);
    }
}
