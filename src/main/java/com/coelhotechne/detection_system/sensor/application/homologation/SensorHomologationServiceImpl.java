package com.coelhotechne.detection_system.sensor.application.homologation;

import com.coelhotechne.detection_system.sensor.application.SensorService;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorStatus;
import com.coelhotechne.detection_system.sensor.domain.homologation.SensorHomologationRecord;
import com.coelhotechne.detection_system.sensor.domain.homologation.SensorSelfTestOutcome;
import com.coelhotechne.detection_system.sensor.exceptions.SensorCommandDeliveryException;
import com.coelhotechne.detection_system.sensor.exceptions.SensorHomologationNotEligibleException;
import com.coelhotechne.detection_system.sensor.exceptions.SensorWithoutAccessKey;
import com.coelhotechne.detection_system.sensor.infrastructure.SensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class SensorHomologationServiceImpl implements SensorHomologationService{
    private final SensorRepository repository;
    private final SensorSelfTestPublisher publisher;
    private final SensorService service;
    private final TransactionTemplate template;
    private final Clock clock;

    private record Started(SensorSelfTestRequest request, SensorHomologationRecord homologationRecord){

    }

    @Override
    @Transactional(readOnly = true)
    public SensorHomologationRecord getHomologation(UUID sensorId) {
        return service.requireSensor(sensorId).getHomologation();
    }

    @Override
    public SensorHomologationRecord requestSelfTest(UUID sensorId) {
    UUID requestId = UUID.randomUUID();

    Started started = template.execute(status -> {
        Sensor sensor = service.requireSensor(sensorId);
        requireEligibleForTest(sensor);
        transition(sensorId, ()-> sensor.getHomologation().startTest(requestId,now()));
        return new Started(new SensorSelfTestRequest(sensor.getUuid(),
                sensor.getZone().getName(),
                sensor.getName(),
                requestId),
                sensor.getHomologation());
    });
    try {
    publisher.publish(started.request());
    }catch (SensorCommandDeliveryException e){
        try {
            template.executeWithoutResult(status -> repository
                    .findById(sensorId)
                    .ifPresent(s -> s.getHomologation().abortTest(requestId)));

        }catch (RuntimeException abortFailuer){
            e.addSuppressed(abortFailuer);
        }
        throw  e;
    }
    log.info("Self-test {} dispatched to sensor {} ",requestId,sensorId);
    return started.homologationRecord();
    }

    @Override
    @Transactional
    public boolean applySelfTestAck(UUID sensorId, UUID requestId,SensorSelfTestOutcome outcome){
        return repository.findById(sensorId).map(s -> s.getHomologation()
                .applyTestResult(requestId,outcome,now())).orElse(false);
    }
    @Override
    @Transactional
    public SensorHomologationRecord approve(UUID sensorId, String approvedBy){
    Sensor sensor = service.requireSensor(sensorId);
    transition(sensorId,() -> sensor.getHomologation().approve(approvedBy,now()));
    log.info("Sensor {} homologation approved by {}", sensorId, approvedBy);
        return sensor.getHomologation();
    }

    @Override
    @Transactional
    public SensorHomologationRecord reject(UUID sensorId, String rejectedBy, String reason) {
        Sensor sensor = service.requireSensor(sensorId);
        transition(sensorId,() -> sensor.getHomologation().reject(rejectedBy,reason,now()));
        log.info("Sensor {} homologation approved by {}", sensorId, rejectedBy);
        return sensor.getHomologation();
    }

    @Override
    @Transactional
    public boolean expireIfOverdue(UUID sensorId, Duration timeout, Instant now) {
        return repository.findById(sensorId)
                .map(sensor -> sensor.getHomologation().expireIfOverdue(now, timeout))
                .orElse(false);
    }

    private void requireEligibleForTest(Sensor sensor) {
        if (sensor.getAccessKey() == null){
            throw new SensorWithoutAccessKey(sensor.getUuid(),"Sensor has no access key issued");
        }if (sensor.getSensorStatus() == SensorStatus.MAINTENANCE_REQUIRED){
            throw new SensorHomologationNotEligibleException(sensor.getUuid(), "Sensor is under maintenance");
        }
    }
    private void transition(UUID sensorId, Runnable runnable) {
    try {
        runnable.run();
    }catch (IllegalStateException e){
        throw new SensorHomologationNotEligibleException(sensorId,e.getMessage());
    }
    }

    private Instant now(){
        return clock.instant();
    }
}
