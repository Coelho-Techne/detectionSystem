package com.coelhotechne.detection_system.device.application.auth;

import com.coelhotechne.detection_system.device.api.dto.auth.DeviceAuthResponse;
import com.coelhotechne.detection_system.device.api.dto.auth.DeviceKeyGenerator;
import com.coelhotechne.detection_system.device.domain.Device;
import com.coelhotechne.detection_system.device.domain.enums.DeviceMessage;
import com.coelhotechne.detection_system.device.exceptions.DeviceAuthenticationException;
import com.coelhotechne.detection_system.device.exceptions.DeviceNotFoundException;
import com.coelhotechne.detection_system.device.infrastructure.DeviceRepository;
import com.coelhotechne.detection_system.globalClass.security.AccessKeyGenerator;
import com.coelhotechne.detection_system.globalClass.security.AccessKeyHasher;
import com.coelhotechne.detection_system.globalClass.security.IssuedAccessKeyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class DeviceAuthServiceImp implements DeviceAuthService{
    private final AccessKeyGenerator keyGenerator;
    private final AccessKeyHasher hasher;
    private final DeviceRepository repository;
    private final Clock clock;

    @Override
    @Transactional
    public DeviceAuthResponse authenticate(UUID uuid, String providedAccessKey) {
        Optional<Device> device = repository.findById(uuid)
                .filter(d -> Boolean.TRUE.equals(d.getStatus()))
                .filter(d->hasher.matches(providedAccessKey,d.getAccessKey()));
        if(device.isEmpty()){
            log.warn("Authentication failed for device {}",uuid);
            return new DeviceAuthResponse(DeviceMessage.DENIED,"Invalid Access Key");
        }
        device.get().setLastCommunication(LocalDateTime.now(clock));
        return new DeviceAuthResponse(DeviceMessage.AUTHORIZED,"Authenticated successfully!");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidAccessKey(UUID uuid, String providedKey) {
     return repository.findById(uuid).filter(d-> Boolean.TRUE.equals(d.getStatus()))
             .map(d->hasher.matches(providedKey,d.getAccessKey()))
             .orElse(false);
    }

    @Override
    public IssuedAccessKeyResponse rotateAccessKey(UUID uuid) {
        Device entity = repository
                .findById(uuid)
                .orElseThrow(()->
                        new DeviceNotFoundException(uuid.toString(), null, "Device id not found!"));
        String newKey = keyGenerator.generate();
        entity.issueAccessKey(newKey);
        log.info("Access key rotated for device: {}",uuid);
        return new IssuedAccessKeyResponse(uuid,newKey,clock.instant());
    }
}