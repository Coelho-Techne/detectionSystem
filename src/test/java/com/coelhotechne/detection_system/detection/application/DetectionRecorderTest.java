package com.coelhotechne.detection_system.detection.application;

import com.coelhotechne.detection_system.cam.infrastructure.CamRepository;
import com.coelhotechne.detection_system.detection.domain.Detection;
import com.coelhotechne.detection_system.detection.infrastructure.DetectionRepository;
import com.coelhotechne.detection_system.sensor.infrastructure.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
}
