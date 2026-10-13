package com.coelhotechne.detection_system.detection.application;

import com.coelhotechne.detection_system.cam.domain.base.BaseCam;
import com.coelhotechne.detection_system.cam.domain.fixed.CamFixed;
import com.coelhotechne.detection_system.cam.exceptions.CamNotFoundException;
import com.coelhotechne.detection_system.cam.infrastructure.CamRepository;
import com.coelhotechne.detection_system.detection.application.enums.DetectionOutcome;
import com.coelhotechne.detection_system.detection.domain.*;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import com.coelhotechne.detection_system.detection.event.DetectionRecordedEvent;
import com.coelhotechne.detection_system.detection.infrastructure.DetectionRepository;
import com.coelhotechne.detection_system.globalClass.time.DeviceClock;
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
import java.util.Optional;
import java.util.UUID;

@Component
public class DetectionRecorder {
    private static final Logger log = LogManager.getLogger(DetectionRecorder.class);

    static final Duration MAX_FUTURE_SKEW = Duration.ofMinutes(5);

    private final DetectionRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final SensorRepository sensorRepository;
    private final CamRepository camRepository;
    private final DetectionMetrics metrics;
    private final Clock clock;
    private final Duration trackDedupWindow;

    public DetectionRecorder(
            DetectionRepository repository,
            SensorRepository sensorRepository,
            CamRepository camRepository,
            ApplicationEventPublisher eventPublisher,
            DetectionMetrics detectionMetrics,
            Clock clock,
            @Value("${detection.camera.track-dedup-window:30s}")Duration trackDedupWindow) {
        this.repository = repository;
        this.sensorRepository = sensorRepository;
        this.camRepository = camRepository;
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
                    null, DetectionOutcome.INVALID,
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

    public DetectionRecordResult recordFromCamera(UUID camId,CamDetectionCommand command){
        UUID sourceEventId = cameraSourceEventId(camId,command.eventId());
        DetectionType type = DetectionType.fromCode(command.type());

        Optional<Detection>existing = repository.findBySourceEventId(sourceEventId);
        if (existing.isPresent()){
            metrics.count(DetectionDeviceType.CAMERA,type,DetectionOutcome.DUPLICATE);
            return DetectionRecordResult.duplicate(existing.get());
        }

        BaseCam cam = camRepository.findById(camId)
                .orElseThrow(()-> new CamNotFoundException(camId.toString()," Camera not found!"));

        if (command.type() == null || command.type().isBlank()){
            return discard(DetectionDeviceType.CAMERA,null,DetectionOutcome.INVALID,"Type is required");
        }

        if (!DetectionTypeRules.allowsCamera(type)){
            return discard(DetectionDeviceType.CAMERA,type,DetectionOutcome.TYPE_NOT_ALLOWED,
                    "Type %s not allowed for cameras".formatted(type));
        }
        if (isBelowThreshold(cam,command.confidence())){
            return discard(DetectionDeviceType.CAMERA,type,DetectionOutcome.BELOW_THRESHOLD,"confidence below camera threshold");
        }
      Instant receivedAt = clock.instant();
      Instant detectedAt = DeviceClock.resolve(command.detectedAt(),receivedAt);

      if (command.trackId()!=null && repository.existsCamTrackSince(
              camId,command.trackId(),type,detectedAt.minus(trackDedupWindow))){
          return discard(DetectionDeviceType.CAMERA,type,DetectionOutcome.TRACK_DEDUPLICATED,
                  "track %d already recorded in the last %s".formatted(command.trackId(),trackDedupWindow));
      }
      CamDetection camDetection;

      try {
          camDetection = new CamDetection(
                  sourceEventId,
                  cam,
                  type,
                  command.type(),
                  detectedAt,
                  receivedAt,
                  command.confidence(),
                  command.trackId(),
                  command.moving(),
                  command.taxon(),
                  BoundingBox.of(command.bbox()),
                  command.snapshotRef(),
                  command.modelVersion()
          );
      }catch (IllegalArgumentException ex){
          return discard(DetectionDeviceType.CAMERA,type,DetectionOutcome.INVALID,ex.getMessage());
      }
      cam.recordCommunication(receivedAt);
      return store(camDetection);
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
