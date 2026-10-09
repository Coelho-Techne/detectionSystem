package com.coelhotechne.detection_system.detection.application;

import com.coelhotechne.detection_system.detection.domain.DetectionTypeRules;
import com.coelhotechne.detection_system.detection.domain.SensorDetection;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import com.coelhotechne.detection_system.detection.event.DetectionRedordedEvent;
import com.coelhotechne.detection_system.detection.infrastructure.DetectionRepository;
import com.coelhotechne.detection_system.sensor.application.SensorService;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.sensor.domain.payload.SensorDetectionPayload;
import com.coelhotechne.detection_system.sensor.event.SensorDetectionEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Component
public class DetectionRecorder {
    private static final Logger log = LogManager.getLogger(DetectionRecorder.class);

    static final Duration MAX_FUTURE_SKEW = Duration.ofMinutes(5);

    private final DetectionRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final SensorService service;

    public DetectionRecorder(
            DetectionRepository repository,
            ApplicationEventPublisher eventPublisher,
            SensorService service) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.service=service;
    }
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public  void onSensorDetection(SensorDetectionEvent event){
        SensorDetectionPayload payload = event.detection();
        if (payload == null){
            log.warn("Empty detection payload from sensor {} - discarded",event.sensorId());
            return;
        }
        if (repository.existsBySourceEventId(event.eventId())){
            log.debug(
                    "Detection {} from Sensor {} already recorded (redelivery) - ignored"
                    ,event.eventId(),event.sensorId());
            return;
        }
        Sensor sensor = service.requireSensor(event.sensorId());
        String rawType = payload.category();
        DetectionType type = DetectionType.fromCode(rawType);
        if (type == DetectionType.UNKNOWN && rawType!=null && !rawType.isBlank()){
            log.info("Sensor {} (niche {}) reported unmapped detection type '{}' — recorded as UNKNOWN",
                    sensor.getUuid(), sensor.getSensorNiche(), rawType);
        }

        if (!DetectionTypeRules.allowsSensor(sensor.getSensorNiche(),type)){
            log.warn("Sensor {} (niche {}) reported '{}' ({}), not allowed for its niche — discarded",
                    sensor.getUuid(), sensor.getSensorNiche(), rawType, type);
            return;
        }

        Instant receivedAt = event.occurredAt();
        Instant detectedAt = resolveDetectedAt(payload.detectedAt(),receivedAt);
        if (!detectedAt.equals(payload.detectedAt())){
            log.debug("Sensor {} sent detected_at={} — using server time {}",
                    sensor.getUuid(), payload.detectedAt(), receivedAt);
        }

        SensorDetection saved = repository.save(new SensorDetection(
                event.eventId(),
                sensor,
                type,
                rawType,
                detectedAt,
                receivedAt,
                payload.description()
        ));
        eventPublisher.publishEvent(DetectionRedordedEvent.of(saved));
    }
    static Instant resolveDetectedAt(Instant reported,Instant recivedAt){
        if (reported!=null|| reported.isAfter(recivedAt.plus(MAX_FUTURE_SKEW))){
            return recivedAt;
        }
        return reported;
    }
}
