package com.coelhotechne.detection_system.sensor.application.event;

import com.coelhotechne.detection_system.batterysupply.domain.PowerSupply;
import com.coelhotechne.detection_system.batterysupply.domain.PowerSupplyStatus;
import com.coelhotechne.detection_system.batterysupply.domain.PowerSupplyThresholds;
import com.coelhotechne.detection_system.batterysupply.domain.PowerSupplyType;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.sensor.domain.SensorDiagnosticsThresholds;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorNiche;
import com.coelhotechne.detection_system.sensor.domain.payload.SensorTelemetryPayload;
import com.coelhotechne.detection_system.sensor.event.SensorPowerSupplyEvent;
import com.coelhotechne.detection_system.sensor.event.SensorTelemetryEvent;
import com.coelhotechne.detection_system.sensor.infrastructure.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SensorEventApplierTest {

    private static final UUID SENSOR_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final Instant TO = Instant.parse("2026-10-09T12:00:00Z");

    private SensorRepository repository;
    private ApplicationEventPublisher publisher;
    private SensorEventApplier applier;
    private Sensor sensor;

    @BeforeEach
    void setUp(){
        repository = mock(SensorRepository.class);
        publisher = mock(ApplicationEventPublisher.class);
        applier = new SensorEventApplier(repository,publisher,
                new SensorDiagnosticsThresholds(null,null),
                new PowerSupplyThresholds(null,null,null));
        sensor = new Sensor();
        sensor.setSensorNiche(SensorNiche.ENVIRONMENT);
        when(repository.findById(SENSOR_ID)).thenReturn(Optional.of(sensor));
        when(repository.saveAndFlush(any(Sensor.class))).thenAnswer(inv -> inv.getArgument(0));
    }
    @Test
    void sensorWithoutPowerSupplyDoesNotThrow(){
        assertThatCode(()-> applier.apply(event(new BigDecimal("50")))).doesNotThrowAnyException();
        verify(repository).saveAndFlush(sensor);
    }
    @Test
    void telemetryIsAlwaysRepublishedForHistory(){
        applier.apply(event(null));
        applier.apply(event(null));

        verify(repository,times(1)).saveAndFlush(sensor);
        verify(publisher,times(2)).publishEvent(any(SensorTelemetryEvent.class));
    }
    @Test
    void batteryStatusChangePublishesPowerEvent(){
        sensor.setPowerSupply(new PowerSupply(PowerSupplyType.RECHARGEABLE_BATTERY,true));

        applier.apply(event(new BigDecimal("10")));

        ArgumentCaptor<Object> published = ArgumentCaptor.forClass(Object.class);
        verify(publisher,atLeastOnce()).publishEvent(published.capture());
        List<SensorPowerSupplyEvent> power = published
                .getAllValues()
                .stream()
                .filter(SensorPowerSupplyEvent.class::isInstance)
                .map(SensorPowerSupplyEvent.class::cast)
                .toList();

        assertThat(power).hasSize(1);
        assertThat(power.get(0).previousStatus()).isEqualTo(PowerSupplyStatus.NORMAL);
        assertThat(power.get(0).newStatus()).isEqualTo(PowerSupplyStatus.CRITICAL);
    }

    private static SensorTelemetryEvent event(BigDecimal battery){
        SensorTelemetryPayload payload = new SensorTelemetryPayload(2,
                null,
                TO,
                true,
                new BigDecimal("10"),new BigDecimal("5"),
                null,
                battery,
                battery == null ? null : false,
                null,
                List.of());
        return new SensorTelemetryEvent(UUID.randomUUID(),SENSOR_ID,payload,TO,TO);
    }
}
