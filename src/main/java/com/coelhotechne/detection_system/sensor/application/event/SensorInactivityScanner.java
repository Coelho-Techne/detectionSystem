package com.coelhotechne.detection_system.sensor.application.event;

import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorStatus;
import com.coelhotechne.detection_system.sensor.event.SensorStatusEvent;
import com.coelhotechne.detection_system.sensor.infrastructure.SensorRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@Log4j2
public class SensorInactivityScanner {

    private final SensorRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate template;
    private final Clock clock;
    private final Duration window;


    public SensorInactivityScanner(SensorRepository repository,
                                   ApplicationEventPublisher eventPublisher,
                                    TransactionTemplate template,
                                   Clock clock,
                                   @Value("${sensor.inactivity.window:5m}")Duration window){
    this.repository=repository;
    this.eventPublisher=eventPublisher;
    this.template = template;
    this.clock=clock;
    this.window=window;
    }
    @Scheduled(fixedDelayString = "${sensor.inactivity.scan-interval-ms:30000}")
    public void markStaleSensorDisconnected(){
        Instant threshold = clock.instant().minus(window);
        List<UUID>stale =repository.findStaleSensors(threshold, SensorStatus.DISCONNECTED).stream()
                .filter(s -> s.getSensorStatus() != SensorStatus.MAINTENANCE_REQUIRED)
                .map(Sensor::getUuid)
                .toList();
        int marked = 0 ;
        for (UUID sensorId : stale){
            try {
                Boolean changed =template.execute(status -> markOne(sensorId,threshold));
                if (Boolean.TRUE.equals(changed)){
                    marked++;
                }
            }catch (OptimisticLockingFailureException ex){
                log.debug("Sensor {} changed during inactivity sweep skipped",sensorId);
            }catch (RuntimeException ex){
                log.warn("Failed to mark sensor {} as disconnected",sensorId,ex);
            }
        }
        if (marked > 0) {
            log.info("Inactivity sweep: {} sensor(s) not reading since {} marked DISCONNECTED", marked, threshold);
        }
    }
    private boolean markOne(UUID sensorId, Instant threshold){
        Sensor sensor = repository.findById(sensorId).orElse(null);
        if (sensor == null || sensor.getSensorStatus() == SensorStatus.MAINTENANCE_REQUIRED
        ||isRecent(sensor.getLastReadingAt(),threshold)
        ||isRecent(sensor.getLastCommunication(),threshold)){
            return false;
        }
        SensorStatus previous = sensor.getSensorStatus();
        if (!sensor.markDisconnected()){
            return false;
        }
        repository.saveAndFlush(sensor);
        eventPublisher.publishEvent(new SensorStatusEvent(
                UUID.randomUUID(),sensorId,previous,SensorStatus.DISCONNECTED,clock.instant()
        ));
        return true;
    }

    private static boolean isRecent(Instant at, Instant threshold){
        return at !=null && !at.isBefore(threshold);
    }

}
