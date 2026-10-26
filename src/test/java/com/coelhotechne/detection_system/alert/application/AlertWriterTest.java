package com.coelhotechne.detection_system.alert.application;

import com.coelhotechne.detection_system.alert.domain.Alert;
import com.coelhotechne.detection_system.alert.domain.AlertDecision;
import com.coelhotechne.detection_system.alert.domain.enums.AlertCategory;
import com.coelhotechne.detection_system.alert.domain.enums.AlertSourceType;
import com.coelhotechne.detection_system.alert.domain.enums.AlertType;
import com.coelhotechne.detection_system.alert.domain.enums.Severity;
import com.coelhotechne.detection_system.alert.event.AlertChangedEvent;
import com.coelhotechne.detection_system.alert.infrastructure.AlertRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class AlertWriterTest {

    private static final Instant NOW = Instant.parse("2026-10-26T17:28:00Z");

    private AlertRepository repository;
    private ApplicationEventPublisher eventPublisher;
    private AlertWriter writer;

    @BeforeEach
    void setUp(){
        repository = mock(AlertRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        writer = new AlertWriter(repository,eventPublisher,new SimpleMeterRegistry(),
                Clock.fixed(NOW, ZoneOffset.UTC), Duration.ofMinutes(5));
    }
    @Test
    void firstOccurrenceIsCreated(){
        AlertDecision d = decision(Severity.MEDIUM);
        givenUpsertReturns(1,d);
        writer.apply(AlertRuleOutcome.raise(d));
        assertThat(publishChange()).isEqualTo(AlertChangedEvent.Change.CREATED);
    }


    private void givenUpsertReturns(int rows,AlertDecision d){
        when(repository.upsertOpen(any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), eq(d.dedupKey()), any())).thenReturn(rows);
        when(repository.findByOpenDedupKey(d.dedupKey())).thenReturn(Optional.of(new Alert(d,null,null)));
    }

    private AlertChangedEvent.Change publishChange(){
        ArgumentCaptor<AlertChangedEvent> captor =ArgumentCaptor.forClass(AlertChangedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        return captor.getValue().change();
    }

    private static AlertDecision decision(Severity severity){
        UUID source = UUID.fromString("11111111-1111-1111-1111-111111111111");
        return new AlertDecision(AlertCategory.DETECTION, AlertType.DETECTED,severity, AlertSourceType.SENSOR,
                source, null, null, "Motion detected", null,
                AlertDecision.key(AlertSourceType.SENSOR,source,AlertType.DETECTED,"MOTION"),NOW);
    }
}
