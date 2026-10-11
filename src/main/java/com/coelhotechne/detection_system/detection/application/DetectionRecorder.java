package com.coelhotechne.detection_system.detection.application;

import com.coelhotechne.detection_system.cam.domain.base.BaseCam;
import com.coelhotechne.detection_system.cam.domain.fixed.CamFixed;
import com.coelhotechne.detection_system.cam.infrastructure.CamFixedRepository;
import com.coelhotechne.detection_system.detection.domain.Detection;
import com.coelhotechne.detection_system.detection.domain.DetectionTypeRules;
import com.coelhotechne.detection_system.detection.domain.SensorDetection;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import com.coelhotechne.detection_system.detection.event.DetectionRecordedEvent;
import com.coelhotechne.detection_system.detection.infrastructure.DetectionRepository;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.sensor.domain.payload.SensorDetectionPayload;
import com.coelhotechne.detection_system.sensor.event.SensorDetectionEvent;
import com.coelhotechne.detection_system.sensor.exceptions.SensorNotFoundException;
import com.coelhotechne.detection_system.sensor.infrastructure.SensorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class DetectionRecorder {
    private static final Logger log = LogManager.getLogger(DetectionRecorder.class);

    static final Duration MAX_FUTURE_SKEW = Duration.ofMinutes(5);

    private final DetectionRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final SensorRepository sensorRepository;
    private final CamFixedRepository camFixedRepository;
    private final DetectionMetrics metrics;
    private final Clock clock;
    private final Duration trackDedupWindow;

    public DetectionRecorder(
            DetectionRepository repository,
            SensorRepository sensorRepository,
            CamFixedRepository camFixedRepository,
            ApplicationEventPublisher eventPublisher,
            DetectionMetrics detectionMetrics,
            Clock clock,
            @Value("${detection.camera.track-dedup-window:30s}")Duration trackDedupWindow) {
        this.repository = repository;
        this.sensorRepository = sensorRepository;
        this.camFixedRepository = camFixedRepository;
        this.eventPublisher = eventPublisher;
        this.metrics= detectionMetrics;
        this.clock = clock;
        this.trackDedupWindow= trackDedupWindow;
    }

    // ::::::::::::::::::::::::::::: SENSOR (MQTT) :::::::::::::::::::::::::::::

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public  void onSensorDetection(SensorDetectionEvent event){recordedFromSensor(event);}


    DetectionRecordResult recordedFromSensor(SensorDetectionEvent event){

        SensorDetectionPayload payload = event.detection();
        if (payload == null || !payload.hasCategory()){
            log.warn("Detection payload from sensor {} - discarded",event.sensorId());
            return discard(DetectionDeviceType.SENSOR,
                    null,DetectionOutcome.INVALID,
                    "category is required");
        }
        DetectionType type = DetectionType.fromCode(payload.category());
        Sensor sensor = sensorRepository.findById(event.sensorId()).orElseThrow(()->
                new SensorNotFoundException(event.sensorId().toString()," Sensor not found!"));

        if (repository.existsBySourceEventId(event.eventId())){
            log.debug(
                    "Detection {} from Sensor {} already recorded (redelivery) - ignored"
                    ,event.eventId(),event.sensorId());
            return DetectionRecordResult.duplicate(null);
        }

        if (type == DetectionType.UNKNOWN){
            log.info("Sensor {} (niche {}) reported unmapped detection type '{}' - recorded as UNKNOWN",
                    sensor.getUuid(), sensor.getSensorNiche(), payload.category());
        }

        if (!DetectionTypeRules.allowsSensor(sensor.getSensorNiche(),type)){
            log.warn("Sensor {} (niche {}) reported '{}' ({}), not allowed for its niche — discarded",
                    sensor.getUuid(), sensor.getSensorNiche(), payload.category(), type);
            return discard(DetectionDeviceType.SENSOR,type,DetectionOutcome.TYPE_NOT_ALLOWED,
                    "type %s not allowed for niche %s".formatted(type, sensor.getSensorNiche()));
        }
        SensorDetection detection;
        try{
            detection = new SensorDetection(
                    event.eventId(),
                    sensor,
                    type,
                    payload.category(),
                    event.detectedAt(),
                    event.occurredAt(),
                    payload.value(),
                    payload.confidence(),
                    payload.unit(),
                    payload.description());
        }catch (IllegalArgumentException ex){
            log.warn("Invalid detection from sensor {}: {} - discarded",
                    sensor.getUuid(), ex.getMessage());
            return discard(DetectionDeviceType.SENSOR,type,DetectionOutcome.INVALID,ex.getMessage());
        }
        return store(detection);
    }




    static Instant resolveDetectedAt(Instant reported,Instant recivedAt){
        if (reported != null|| reported.isAfter(recivedAt.plus(MAX_FUTURE_SKEW))){
            return recivedAt;
        }
        return reported;
    }

    // ::::::::::::::::::::::::::::: COMUM :::::::::::::::::::::::::::::

    private DetectionRecordResult store(Detection detection){
        Detection saved = repository.save(detection);
        metrics.count(saved.getDeviceType(),saved.getType(),DetectionOutcome.STORED);
        if (!saved.isSourceHomologated()){
            log.debug("Detection {} recorded from non-homologated {} {}"
                    ,saved.getUuid(),
                    saved.getDeviceType(),
                    saved.getSourceEventId());
        }
        eventPublisher.publishEvent(DetectionRecordedEvent.of(saved));
        return DetectionRecordResult.stored(saved);

    }
    private DetectionRecordResult discard(DetectionDeviceType deviceType, DetectionType type,
                                          DetectionOutcome outcome, String reason) {
        metrics.count(deviceType, type, outcome);
        return DetectionRecordResult.discarded(outcome, reason);
    }

    private static boolean isBelowThreshold(BaseCam cam, BigDecimal confidence){
        if (confidence == null ||
                !(cam instanceof CamFixed fixed) ||
                fixed.getDetectionConfidenceThreshold() == null){
            return false;
        }
        return confidence.compareTo(BigDecimal.valueOf(fixed.getDetectionConfidenceThreshold()))<0;
    }

    static UUID cameraSourceEventId(UUID camId, UUID eventId){
        return UUID.nameUUIDFromBytes((camId + "|"+eventId).getBytes(StandardCharsets.UTF_8));
    }
}
