package com.coelhotechne.detection_system.alert.application;

import com.coelhotechne.detection_system.alert.domain.Alert;
import com.coelhotechne.detection_system.alert.domain.AlertDecision;
import com.coelhotechne.detection_system.alert.domain.enums.AlertStatus;
import com.coelhotechne.detection_system.alert.domain.enums.Severity;
import com.coelhotechne.detection_system.alert.event.AlertChangedEvent;
import com.coelhotechne.detection_system.alert.infrastructure.AlertRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
@Log4j2
public class AlertWriter {

    private final AlertRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;
    private final Clock clock;
    private final Duration silenceWindow;

    public AlertWriter(
            AlertRepository repository,
            ApplicationEventPublisher eventPublisher,
            MeterRegistry meterRegistry,
            Clock clock,
            @Value("${alert.dedup.window:5m}")
            Duration silenceWindow
    ){
        this.repository=repository;
        this.eventPublisher=eventPublisher;
        this.meterRegistry=meterRegistry;
        this.clock=clock;
        this.silenceWindow = silenceWindow;
    }
    @Transactional(propagation = Propagation.MANDATORY)
    public void apply(AlertRuleOutcome outcome){
        Instant now = clock.instant();
        for (String prefix : outcome.clearPrefixes()){
            clear(prefix,now);
        }
        if (outcome.raise()!=null){
            raise(outcome.raise(),now);
        }
    }

    private void raise(AlertDecision decision, Instant now){
        if (decision.severity().compareTo(Severity.HIGH)<0 && repository
                .existsByDedupKeyAndClosedAtAfter(decision.dedupKey(),now.minus(silenceWindow))){
            log.debug("Alert {} suppressed: same key closed less than {} ago",decision.dedupKey(),silenceWindow);
            count("supressed",decision);
            return;
        }
        int rows = repository.upsertOpen(
                UUID.randomUUID(),
                now,
                decision.category().name(),
                decision.type().name(),
                decision.severity().name(),
                decision.sourceType().name(),
                decision.sourceId(),
                decision.detectionId(),
                decision.zoneId(),
                decision.title(),
                decision.description(),
                decision.dedupKey(),
                decision.occurredAt()
        );
        Alert alert = repository.findByOpenDedupKey(decision.dedupKey()).orElseThrow(()->
                new IllegalArgumentException("Open alert not found after upsert"+ decision.dedupKey()));

        boolean created = rows == 1;
        count(created ? "created": "repeated",decision);
        if (created){
            log.info("Alert {} raised: {} {} ({}) ",alert.getUuid(),decision.severity()
                    ,decision.title(),decision.dedupKey());
        }
        publish(alert,created ? AlertChangedEvent.Change.CREATED: AlertChangedEvent.Change.REPEATED);
    }

    private void clear(String keyPrefix,Instant now){
        List<Alert> open = repository.findOpenByKeyPrefix(keyPrefix, AlertStatus.OPEN);
        for (Alert alert: open){
          alert.clear(now);
          log.info("Alert {} cleared: condition '{}' is gone",
                  alert.getUuid(),
                  alert.getDedupKey());
          publish(alert,AlertChangedEvent.Change.CLEARED);
        }
    }

    private void publish(Alert alert, AlertChangedEvent.Change change){
        eventPublisher.publishEvent(new AlertChangedEvent(
                alert.getUuid(),change,alert.getStatus(),alert.getSeverity(),
                alert.getZone() == null ? null : alert.getZone().getUuid()));
    }
    private void count(String result, AlertDecision decision){
        meterRegistry.counter("detection.alert.raised",
                "result",result,
                "severity",decision.severity().name().toLowerCase(),
                "category",decision.category().name().toLowerCase()).increment();
    }
}
