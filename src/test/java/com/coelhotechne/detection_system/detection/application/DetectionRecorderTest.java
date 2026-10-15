package com.coelhotechne.detection_system.detection.application;

import com.coelhotechne.detection_system.cam.infrastructure.CamRepository;
import com.coelhotechne.detection_system.detection.application.enums.DetectionOutcome;
import com.coelhotechne.detection_system.detection.domain.Detection;
import com.coelhotechne.detection_system.detection.domain.SensorDetection;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import com.coelhotechne.detection_system.detection.event.DetectionRecordedEvent;
import com.coelhotechne.detection_system.detection.infrastructure.DetectionRepository;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorNiche;
import com.coelhotechne.detection_system.sensor.domain.payload.SensorDetectionPayload;
import com.coelhotechne.detection_system.sensor.event.SensorDetectionEvent;
import com.coelhotechne.detection_system.sensor.infrastructure.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DetectionRecorderTest {
    private static final Instant NOW = Instant.parse("2026-10-09T23:41:05Z");
    private static final UUID SENSOR_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private DetectionRepository repository;
    private SensorRepository sensorRepository;
    private ApplicationEventPublisher publisher;
    private DetectionMetrics metrics;
    private DetectionRecorder recorder;

    @BeforeEach
    void setUp(){
        repository = mock(DetectionRepository.class);
        sensorRepository = mock(SensorRepository.class);
        publisher = mock(ApplicationEventPublisher.class);
        metrics=mock(DetectionMetrics.class);
        recorder = new DetectionRecorder(
                repository,
                sensorRepository,
                mock(CamRepository.class),
                publisher,
                metrics,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofSeconds(30)
        );
        when(repository.save(any(Detection.class))).thenAnswer(inv -> inv.getArgument(0));
    }
    @Test
    void storesAndPublish(){
        givenSensor(SensorNiche.GAS);
        DetectionRecordResult recordResult = recorder.recordedFromSensor(event("gas_leak",new BigDecimal("850"),"ppm"));

        assertThat(recordResult.outcome()).isEqualTo(DetectionOutcome.STORED);
        ArgumentCaptor<SensorDetection>saved = ArgumentCaptor.forClass(SensorDetection.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getType()).isEqualTo(DetectionType.GAS_LEAK);
        assertThat(saved.getValue().getMeasuredValue()).isEqualByComparingTo("850");
        assertThat(saved.getValue().getMeasuredUnit()).isEqualTo("ppm");
        assertThat(saved.getValue().getDetectedAt()).isEqualTo(NOW.minusSeconds(5));
        assertThat(saved.getValue().isSourceHomologated()).isFalse();
        verify(publisher,times(1)).publishEvent(any(DetectionRecordedEvent.class));
    }
    @Test
    void redeliveryIsIgnored(){
        when(repository.existsBySourceEventId(any())).thenReturn(true);
        DetectionRecordResult result = recorder.recordedFromSensor(event("gas_leak",
                null,null));
        assertThat(result.outcome()).isEqualTo(DetectionOutcome.DUPLICATE);
        verify(repository,never()).save(any());
        verifyNoInteractions(publisher);
    }
    @Test
    void typeNotAllowedForNicheIsDiscardedAndCounted(){
        givenSensor(SensorNiche.HYDRAULIC);

        DetectionRecordResult result = recorder.recordedFromSensor(event(
                "smoke",
                null,
                null
        ));

        assertThat(result.outcome()).isEqualTo(DetectionOutcome.TYPE_NOT_ALLOWED);
        verify(repository,never()).save(any());
        verify(metrics).count(any(),any(), ArgumentMatchers.eq(DetectionOutcome.TYPE_NOT_ALLOWED));
    }
    @Test
    void blankCetegoryIsInvalid(){
        DetectionRecordResult result = recorder.recordedFromSensor(event(" ",null,null));
        assertThat(result.outcome()).isEqualTo(DetectionOutcome.INVALID);
        verifyNoInteractions(sensorRepository);
    }

    @Test
    void unknownCodeIsStoredAsUnknown(){
        givenSensor(SensorNiche.SECURITY);

        DetectionRecordResult result = recorder.recordedFromSensor(event("laser_tripwire",null,null));

        assertThat(result.outcome()).isEqualTo(DetectionOutcome.STORED);
        assertThat(result.detection().getType()).isEqualTo(DetectionType.UNKNOWN);
        assertThat(result.detection().getUnmappedType()).isEqualTo("laser_tripwire");
    }


    private void givenSensor(SensorNiche niche){
        Sensor sensor = new Sensor();
        sensor.setSensorNiche(niche);
        when(sensorRepository.findById(SENSOR_ID)).thenReturn(Optional.of(sensor));
    }
    private static SensorDetectionEvent event(String category, BigDecimal value, String unit){
        SensorDetectionPayload payload = new SensorDetectionPayload(
                2,
                UUID.randomUUID(),
                category,
                null,
                NOW.minusSeconds(5),
                null,
                value,
                unit
        );

        return new SensorDetectionEvent(
                UUID.randomUUID(),
                SENSOR_ID,
                payload,
                NOW,
                NOW.minusSeconds(5)
        );
    }
}
