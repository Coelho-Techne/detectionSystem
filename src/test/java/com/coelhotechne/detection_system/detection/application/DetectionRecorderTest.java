package com.coelhotechne.detection_system.detection.application;

import com.coelhotechne.detection_system.cam.infrastructure.CamRepository;
import com.coelhotechne.detection_system.detection.application.enums.DetectionOutcome;
import com.coelhotechne.detection_system.detection.domain.Detection;
import com.coelhotechne.detection_system.detection.domain.SensorDetection;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionGasType;
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
    private static final Instant DEVICE_TIME = NOW.minusSeconds(5);
    private static final UUID SENSOR_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private DetectionRepository repository;
    private SensorRepository sensorRepository;
    private CamRepository camRepository;
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
    //::::::::::::::::::::::::::::: SENSOR - fluxo basico :::::::::::::::::::::::::::::

    @Test
    void storesAndPublish(){
        givenSensor(SensorNiche.GAS);
        DetectionRecordResult recordResult = recorder.recordedFromSensor(event(payload(
                "gas_leak",
                null,
                new BigDecimal("850"),
                "ppm",
                null,
                DEVICE_TIME)));

        assertThat(recordResult.outcome()).isEqualTo(DetectionOutcome.STORED);
        SensorDetection saved = captureSaved();
        assertThat(saved.getType()).isEqualTo(DetectionType.GAS_LEAK);
        assertThat(saved.getGasType()).isNull();
        assertThat(saved.getMeasuredValue()).isEqualByComparingTo("850");
        assertThat(saved.getMeasuredUnit()).isEqualTo("ppm");
        assertThat(saved.getDetectedAt()).isEqualTo(DEVICE_TIME);
        assertThat(saved.getReceivedAt()).isEqualTo(NOW);
        assertThat(saved.isSourceHomologated()).isFalse();
        verify(metrics).count(DetectionDeviceType.SENSOR,DetectionType.GAS_LEAK,DetectionOutcome.STORED);
        verify(publisher).publishEvent(any(DetectionRecordedEvent.class));
    }
    @Test
    void redeliveryReturnsExistingAndCountsDuplicate(){
        SensorDetection existing = storedGasLeak();
        when(repository.findBySourceEventId(any())).thenReturn(Optional.of(existing));

        DetectionRecordResult result = recorder.recordedFromSensor(event(payload("gas_leak",null)));

        assertThat(result.outcome()).isEqualTo(DetectionOutcome.DUPLICATE);
        assertThat(result.detection()).isSameAs(existing);
        verify(metrics).count(DetectionDeviceType.SENSOR,DetectionType.GAS_LEAK,DetectionOutcome.DUPLICATE);
        verify(repository,never()).save(any());
        verifyNoInteractions(sensorRepository,publisher);
    }

    @Test
    void typeNotAllowedForNicheIsDiscardedAndCounted(){
        givenSensor(SensorNiche.HYDRAULIC);

        DetectionRecordResult result = recorder.recordedFromSensor(event(payload("smoke",null)));

        assertThat(result.outcome()).isEqualTo(DetectionOutcome.TYPE_NOT_ALLOWED);
        verify(repository,never()).save(any());
        verify(metrics).count(DetectionDeviceType.SENSOR,DetectionType.SMOKE,DetectionOutcome.TYPE_NOT_ALLOWED);
    }

    @Test
    void blankCategoryIsInvalid(){
        DetectionRecordResult result = recorder.recordedFromSensor(event(payload(" ",null)));

        assertThat(result.outcome()).isEqualTo(DetectionOutcome.INVALID);
        verify(metrics).count(DetectionDeviceType.SENSOR,null,DetectionOutcome.INVALID);
        verifyNoInteractions(sensorRepository,repository);
    }

    @Test
    void unknownCodeIsStoredAsUnknown(){
        givenSensor(SensorNiche.SECURITY);

        DetectionRecordResult result = recorder.recordedFromSensor(event(payload("laser_tripwire",null)));

        assertThat(result.outcome()).isEqualTo(DetectionOutcome.STORED);
        assertThat(result.detection().getType()).isEqualTo(DetectionType.UNKNOWN);
        assertThat(result.detection().getUnmappedType()).isEqualTo("laser_tripwire");
    }

    //::::::::::::::::::::::::::::: SENSOR - DEVICE CLOCK :::::::::::::::::::::::::::::
    @Test
    void deviceTimeInTheFutureFallsBackToReceivedAt(){
        givenSensor(SensorNiche.GAS);

        recorder.recordedFromSensor(event(payload("gas_leak",
                null,
                null,
                null,
                null,
                NOW.plus(Duration.ofHours(1)))));
        assertThat(captureSaved().getReceivedAt()).isEqualTo(NOW);
    }
    @Test
    void missingDeviceTimeFallsBackToReceivedAt(){
        givenSensor(SensorNiche.GAS);

        recorder.recordedFromSensor(event(payload("gas_leak",null,null,null,null,null)));
        assertThat(captureSaved().getDetectedAt()).isEqualTo(NOW);
    }


    //::::::::::::::::::::::::::::: SENSOR - GAS :::::::::::::::::::::::::::::

    @Test
    void gasCategoryFromGasSensorIsStoredAsToxicGasWithGasType() {
        givenSensor(SensorNiche.GAS);
        DetectionRecordResult result = recorder.recordedFromSensor(event(payload("carbon_monoxide", null)));
        assertThat(result.outcome()).isEqualTo(DetectionOutcome.STORED);
        SensorDetection saved = captureSaved();
        assertThat(saved.getType()).isEqualTo(DetectionType.TOXIC_GAS);
        assertThat(saved.getGasType()).isEqualTo(DetectionGasType.CARBON_MONOXIDE);
    }

    @Test
    void explicitGasOverridesCategory() {
        givenSensor(SensorNiche.GAS);

        recorder.recordedFromSensor(event(payload("gas_leak","carbon_monoxide")));

        SensorDetection saved = captureSaved();
        assertThat(saved.getType()).isEqualTo(DetectionType.TOXIC_GAS);
        assertThat(saved.getGasType()).isEqualTo(DetectionGasType.CARBON_MONOXIDE);
    }
    @Test
    void unknownExplicitGasIsStillStoredAsGasLeak(){
        givenSensor(SensorNiche.GAS);

        DetectionRecordResult result = recorder.recordedFromSensor(event(payload("gas_leak","unlisted_gas")));

        assertThat(result.outcome()).isEqualTo(DetectionOutcome.STORED);
        SensorDetection saved = captureSaved();
        assertThat(saved.getType()).isEqualTo(DetectionType.GAS_LEAK);
        assertThat(saved.getGasType()).isEqualTo(DetectionGasType.UNKNOWN);
    }

    @Test
    void nonGasSensorNeverCarriesGasType() {
      SensorDetectionPayload co = payload("carbon_monoxide","carbon_monoxide");
      assertThat(DetectionRecorder.resolvGas(sensorOf(SensorNiche.AIR_QUALITY),co)).isNull();

      givenSensor(SensorNiche.SECURITY);
      recorder.recordedFromSensor(event(co));

      SensorDetection saved = captureSaved();
      assertThat(saved.getType()).isEqualTo(DetectionType.UNKNOWN);
      assertThat(saved.getGasType()).isNull();
    }

    @Test
    void measuredValueAndConfidenceAreNotSwapped() {
        givenSensor(SensorNiche.GAS);

        recorder.recordedFromSensor(event(payload("gas_leak", null, new BigDecimal("850"), "ppm", new BigDecimal("0.9"), DEVICE_TIME)));

        SensorDetection saved = captureSaved();
        assertThat(saved.getMeasuredValue()).isEqualByComparingTo("850");
        assertThat(saved.getConfidence()).isEqualByComparingTo("0.9");
    }


    @Test
    void resolveSensorTypePrefersGas() {
        assertThat(DetectionRecorder.resolveSensorType("gas_leak", DetectionGasType.CARBON_MONOXIDE)).isEqualTo(DetectionType.TOXIC_GAS);
        assertThat(DetectionRecorder.resolveSensorType("flood", null)).isEqualTo(DetectionType.FLOOD);
    }

    //:::::::::::::::::::::::::::::CAMERA - input validation:::::::::::::::::::::::::::::

    @Test
    void cameraEventWithoutEventIdIsInvalidAndTouchesNothing(){
        CamDetectionCommand command = mock(CamDetectionCommand.class);
        when(command.type()).thenReturn("person");

        DetectionRecordResult result = recorder.recordFromCamera(UUID.randomUUID(),command);

        assertThat(result.outcome()).isEqualTo(DetectionOutcome.INVALID);
        verify(metrics).count(DetectionDeviceType.CAMERA,null,DetectionOutcome.INVALID);
        verifyNoInteractions(repository,camRepository);
    }
    @Test
    void cameraEventWithBlankTypeIsInvalid(){
        CamDetectionCommand command = mock(CamDetectionCommand.class);
        when(command.eventId()).thenReturn(UUID.randomUUID());
        when(command.type()).thenReturn("  ");

        DetectionRecordResult result = recorder.recordFromCamera(UUID.randomUUID(),command);

        assertThat(result.outcome()).isEqualTo(DetectionOutcome.INVALID);
        verifyNoInteractions(repository,camRepository);
    }

    //::::::::::::::::::::::::::::: HELPERS  :::::::::::::::::::::::::::::

    private SensorDetection captureSaved(){
        ArgumentCaptor<SensorDetection> captor = ArgumentCaptor.forClass(SensorDetection.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }
    private void givenSensor(SensorNiche niche){
        when(sensorRepository.findById(SENSOR_ID)).thenReturn(Optional.of(sensorOf(niche)));
    }
    private static Sensor sensorOf(SensorNiche sensorNiche){
        Sensor sensor = new Sensor();
        sensor.setSensorNiche(sensorNiche);
        return sensor;
    }

    private static SensorDetection storedGasLeak(){
     return new SensorDetection(UUID.randomUUID(),sensorOf(SensorNiche.GAS),DetectionType.GAS_LEAK,"gas_leak",DEVICE_TIME,NOW,null,null,null,null);
    }

    private static SensorDetectionPayload payload(String category, String gas){
        return payload(category,gas,null, null,null,DEVICE_TIME);
    }

    private static SensorDetectionPayload payload(
            String category,
            String gas,
            BigDecimal value,
            String unit,
            BigDecimal confidence,
            Instant detectedAt){
        return new SensorDetectionPayload(
                2,
                UUID.randomUUID(),
                category,
                null,
                detectedAt,
                confidence,
                value,
                unit,
                gas
        );
    }
    private static SensorDetectionEvent event(SensorDetectionPayload payload){

        return new SensorDetectionEvent(
                UUID.randomUUID(),
                SENSOR_ID,
                payload,
                NOW,
                payload.detectedAt()
        );
    }
}
