package com.coelhotechne.detection_system.sensor.application;

import com.coelhotechne.detection_system.installation.application.InstallationResolver;
import com.coelhotechne.detection_system.sensor.api.dto.*;
import com.coelhotechne.detection_system.sensor.application.event.SensorEventProcessor;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorCommand;
import com.coelhotechne.detection_system.sensor.event.SensorEvent;
import com.coelhotechne.detection_system.sensor.exceptions.*;
import com.coelhotechne.detection_system.sensor.mqtt.MqttSensorClient;
import com.coelhotechne.detection_system.sensor.infrastructure.SensorRepository;
import com.coelhotechne.detection_system.zone.application.ZoneService;
import com.coelhotechne.detection_system.zone.domain.Zone;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Log4j2
@Service
@AllArgsConstructor
public class SensorServiceImpl implements SensorService {

    private final SensorRepository repository;
    private final SensorMapper mapper;
    private final ZoneService zoneService;
    private final MqttSensorClient mqttSensorClient;
    private final SensorEventProcessor eventProcessor;
    private final InstallationResolver installationResolver;

    @Override
    public Sensor requireSensor(UUID sensorId) {
        Objects.requireNonNull(sensorId,"Sensor cannot be null");
        return repository.findById(sensorId)
                .orElseThrow(() -> new SensorNotFoundException(sensorId.toString(),"Sensor not found."));
    }
    @Override
    @Transactional(readOnly = true)
    public Page<SensorResponse> findSensorPageList(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SensorResponse findSensorId(UUID uuid) {
        return repository
                .findById(uuid)
                .map(mapper::toResponse)
                .orElseThrow(()->{
            log.error("Sensor with id: {} not find ",uuid);
            return new SensorNotFoundException(uuid.toString(),"Sensor not found!");
        });
    }

    @Override
    @Transactional
    public SensorResponse replaceSensor(UUID uuid, SensorReplaceRequest request, Long expectedVersion) {
        Sensor sensor = requireSensor(uuid);

        if (expectedVersion == null) {
            throw new SensorPreconditionRequiredException(uuid.toString());
        }
        requireVersion(sensor, expectedVersion);

        Zone zone = zoneService.requireZone(request.zoneUUID());
        mapper.applyReplace(sensor, request,
                zone,installationResolver.resolve(request.installation()));
        return saveOrConflict(sensor, uuid);
    }

    @Override
    @Transactional
    public SensorResponse createSensor(SensorRequest sensorRequest) {
        Sensor entity = mapper.toEntity(sensorRequest);
        Zone zone = zoneService.requireZone(sensorRequest.zoneUUID());
        entity.setActivationTime(LocalDateTime.now());
        entity.setLastCommunication(Instant.now());
        entity.setInstallation(installationResolver.resolve(sensorRequest.installation()));
        entity.setZone(zone);
        Sensor created = repository.save(entity);
        return mapper.toResponse(created);
    }

    @Override
    @Transactional
    public SensorResponse patchSensor(UUID uuid, SensorPatchRequest patch, Long expectedVersion) {
        if (patch.isEmpty()) {
            throw new SensorPatchEmptyException(uuid.toString());
        }

        Sensor sensor = requireSensor(uuid);
        requireVersion(sensor, expectedVersion);

        Zone zone = patch.zoneUUID() == null ? null : zoneService.requireZone(patch.zoneUUID());
        mapper.applyPatch(sensor, patch, zone,installationResolver.resolve(patch.installation()));
        return saveOrConflict(sensor, uuid);
    }

    /*@Override
    @Transactional
    public SensorResponse updateSensor(UUID , SensorRequest sensorRequest) {
        Sensor updated = requireSensor(uuid);
        Zone zone = zoneService.requireZone(sensorRequest.zoneUUID());

        if (updated.getSensorStatus().isOperational()) {
            throw new SensorStillActiveException(uuid.toString(), true,
                    "Sensor is currently active (%s), deactivate before updating".formatted(updated.getSensorStatus()));
        }
        if (sensorRequest.installation()!=null){
            updated.setInstallation(sensorRequest.installation());
        }
        if (sensorRequest.zoneUUID()!=null){
            updated.setZone(zone);
        }

        updated.setName(sensorRequest.name());
        updated.setSensorNiche(sensorRequest.sensorNiche());
        updated.setDataDescription(sensorRequest.dataDescription());
        updated.setLastCommunication(Instant.now());
        try {
            Sensor saved = repository.saveAndFlush(updated);
            return mapper.toResponse(saved);
        }catch (ObjectOptimisticLockingFailureException ex){
            throw new SensorConcurrentModificationException(uuid.toString(),ex);
        }
    }*/

    @Override
    @Transactional
    public SensorResponse deleteSensor(UUID uuid) {
        Sensor deleted =requireSensor(uuid);

        if (deleted.getSensorStatus().isOperational()) {
            throw new SensorStillActiveException(uuid.toString(), true,
                    "Sensor is currently active (%s), deactivate before deleting".formatted(deleted.getSensorStatus()));
        }

        repository.delete(deleted);
        return mapper.toResponse(deleted);
    }


    //::::::::::::::::::::::::::::::::::::::::::Intern processing:::::::::::::::::::::::::::::::::::::::
    @Override
    @Transactional
    public SensorResponse requestMaintenance(UUID uuid) {
        Sensor sensor = repository.findById(uuid)
                .orElseThrow(() -> new SensorNotFoundException(uuid.toString(), "Sensor not found!"));
        sensor.requestMaintenance();
        return saveOrConflict(sensor, uuid);
    }
    @Override
    @Transactional
    public SensorResponse clearMaintenance(UUID uuid) {
        Sensor sensor = requireSensor(uuid);
        sensor.clearMaintenance();
        return saveOrConflict(sensor, uuid);
    }

    @Override
    public void process(SensorEvent event) {
        eventProcessor.process(event);
    }

    @Override
    public void sendCommand(UUID uuid, SensorCommand sensorCommand) {
        Objects.requireNonNull(sensorCommand,"Command cannot be null");
        if (sensorCommand == SensorCommand.SELF_TEST){
            throw new SensorCommandNotAllowedException(uuid,sensorCommand,
                    "Use POST /api/v1/sensor/{id}/homologation/test to run the self-test");
        }
        Sensor sensor = requireSensor(uuid);
        if (sensor.getZone() == null) {
            throw new SensorWithoutZoneException(uuid.toString(), "Zone from id: " + uuid + " not found!");
        }

        String topic = "home/%s/%s/command".formatted(sensor.getZone().getName(), sensor.getName());

        try {
            mqttSensorClient.publishCommand(topic, sensorCommand.wireValue());
        } catch (MqttException e) {
            throw new SensorCommandDeliveryException(uuid, e);
        }
    }

    private void requireVersion(Sensor sensor, Long expectedVersion) {
        if (expectedVersion != null && !expectedVersion.equals(sensor.getVersion())) {
            throw new SensorPreconditionFailedException(
                    sensor.getUuid().toString(), String.valueOf(expectedVersion), sensor.getVersion());
        }
    }

    private SensorResponse saveOrConflict(Sensor sensor, UUID uuid) {
        try {
            return mapper.toResponse(repository.saveAndFlush(sensor));
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new SensorConcurrentModificationException(uuid.toString(), ex);
        }
    }

}

