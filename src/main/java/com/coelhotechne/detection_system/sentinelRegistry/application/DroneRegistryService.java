package com.coelhotechne.detection_system.sentinelRegistry.application;

import com.coelhotechne.detection_system.batterysupply.domain.PowerSupply;
import com.coelhotechne.detection_system.device.api.dto.auth.DeviceKeyGenerator;
import com.coelhotechne.detection_system.device.infrastructure.DeviceAlertSubscriptionRepository;
import com.coelhotechne.detection_system.globalClass.security.AccessKeyHasher;
import com.coelhotechne.detection_system.security.event.SessionRevocationEvent;
import com.coelhotechne.detection_system.sentinelRegistry.api.dto.*;
import com.coelhotechne.detection_system.sentinelRegistry.domain.Drone;
import com.coelhotechne.detection_system.sentinelRegistry.exceptions.DroneInactiveException;
import com.coelhotechne.detection_system.sentinelRegistry.exceptions.DroneNotFoundException;
import com.coelhotechne.detection_system.sentinelRegistry.exceptions.DroneSerialConflictException;
import com.coelhotechne.detection_system.sentinelRegistry.infrastructure.DroneFlightEventRepository;
import com.coelhotechne.detection_system.sentinelRegistry.infrastructure.DroneLiveStateRepository;
import com.coelhotechne.detection_system.sentinelRegistry.infrastructure.DroneRepository;
import com.coelhotechne.detection_system.sentinelRegistry.infrastructure.DroneStatusReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;


@Log4j2
@Service
@RequiredArgsConstructor
public class DroneRegistryService implements DroneRegistry{

    private final Clock clock;
    private final DroneMapper mapper;
    private final DroneRepository repository;
    private final AccessKeyHasher accessKeyHasher;
    private final DeviceKeyGenerator keyGenerator;
    private final ApplicationEventPublisher eventPublisher;
    private final DroneLiveStateRepository liveStateRepository;
    private final DroneFlightEventRepository flightEventRepository;
    private final DroneStatusReportRepository statusReportRepository;

    @Override
    public Optional<RegisteredDrone> findDrone(UUID droneId) {
        if (droneId == null){
            return Optional.empty();
        }
        return repository.findById(droneId).map(mapper::toRegistered);
    }

    private final DeviceAlertSubscriptionRepository subscriptionRepository;
    @Override
    @Transactional
    public DroneCreatedResponse registerDrone(DroneRegisterRequest request){
        Objects.requireNonNull(request,"request is required");
        String serial = Drone.normalizeSerial(request.serialNumber());
        if (repository.existsBySerialNumber(serial)){
            throw new DroneSerialConflictException(serial);
        }
        PowerSupply powerSupply = request.powerSupply();

        if (powerSupply == null || powerSupply.getType() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"power_supply.type is required");
        }
        Drone drone = new Drone();
        drone.assignSerialNumber(serial);
        mapper.apply(drone,request);
        String rawKey = keyGenerator.generate();
        drone.issueAccessKey(accessKeyHasher.hash(rawKey));
        drone.setLastCommunication(LocalDateTime.now(clock));
        Drone saved = repository.saveAndFlush(drone);
        log.info("Drone {} registered (serial: {})",saved.getUuid(),saved.getSerialNumber());
        return new DroneCreatedResponse(
                mapper.toResponse(saved),
                rawKey
        );
    }
    @Transactional(readOnly = true)
    public Page<DroneResponse>listPage(Pageable pageable){
        return repository.findAll(pageable).map(mapper::toResponse);
    }
    // Para sistemas que não pageam dados

    @Transactional(readOnly = true)
    public List<DroneResponse>listAll(){
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DroneResponse getDrone(UUID droneId){
    return mapper.toResponse(requireDrone(droneId));
    }

    @Override
    @Transactional
    public DroneResponse updateDrone(UUID droneId, DroneUpdateRequest request){
        Drone drone = requireDrone(droneId);
        boolean wasActive = drone.isActive();
        mapper.apply(drone,request);
        if (wasActive && !drone.isActive()){
            eventPublisher.publishEvent(SessionRevocationEvent.of(droneId.toString(),"drone disabled"));
        }
        return mapper.toResponse(repository.saveAndFlush(drone));
    }

    @Override
    @Transactional
    public void deleteDrone(UUID droneId){
        Drone drone = requireDrone(droneId);
        subscriptionRepository.deleteByDeviceUuid(droneId);
        int reports = statusReportRepository.deleteByDrone(droneId);
        int events = flightEventRepository.deleteByDrone(droneId);
        liveStateRepository.deleteById(droneId);
        repository.delete(drone);
        log.info("Drone {}: {} status report(s) and {} event(s) removed with it",droneId,reports,events);
        eventPublisher.publishEvent(SessionRevocationEvent.of(droneId.toString(),"drone deleted"));
        log.info("Drone : {} deleted",droneId);
    }

    @Override
    @Transactional(readOnly = true)
    public RegisteredDrone requireActive(UUID droneId) {
        Drone drone = requireDrone(droneId);
        if (!drone.isActive()){
            throw new DroneInactiveException(droneId);
        }
        return mapper.toRegistered(drone);
    }
    @Override
    public Drone requireDrone(UUID droneId){
        if (droneId == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"drone id is required");
        }

        return repository.findById(droneId).orElseThrow(()->new DroneNotFoundException(droneId));
    }
}
