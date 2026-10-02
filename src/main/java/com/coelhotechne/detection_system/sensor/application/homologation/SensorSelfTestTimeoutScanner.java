package com.coelhotechne.detection_system.sensor.application.homologation;

import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorStatus;
import com.coelhotechne.detection_system.sensor.domain.homologation.SensorSelfTestOutcome;
import com.coelhotechne.detection_system.sensor.domain.homologation.enums.SensorHomologationStatus;
import com.coelhotechne.detection_system.sensor.infrastructure.SensorRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@Log4j2
public class SensorSelfTestTimeoutScanner {

    private final SensorRepository repository;
    private final Duration timeout;
    private final SensorHomologationService homologationService;

    public SensorSelfTestTimeoutScanner(SensorRepository repository,
                                        SensorHomologationService homologationService,
                                        @Value("${sensor.homologation.self-test-timeout:60s}")Duration timeout){
        this.repository=repository;
        this.timeout = timeout;
        this.homologationService=homologationService;
    }
    @Scheduled(fixedDelayString = "${sensor.homologation.scan-interval-ms:15000}")
    @Transactional
    public void expireOverdueSelfTests(){
        Instant now = Instant.now();
        List<UUID> overdue =repository
                .findOverdueSelfTestIds(SensorHomologationStatus.AWAITING_RESPONSE,now.minus(timeout));
        int expired = 0;

        for(UUID sensorId:overdue){
            try {
                if (homologationService.expireIfOverdue(sensorId, timeout, now)){
                    expired++;
                }
            }catch (ObjectOptimisticLockingFailureException e){
                log.debug("Self-test of sensor {} changed concurrently (ack likely won); skipping"
                        ,sensorId);
            }catch (RuntimeException e){
                log.warn("Failed to expire self-test of sensor: {}",sensorId,e);
            }
        }
        if (expired > 0) {
            log.info("Self-test timeout sweep: {} sensor(s) expired", expired);
        }
    }
}
