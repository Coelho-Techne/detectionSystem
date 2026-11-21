package com.coelhotechne.detection_system.telemetry.application;


import com.coelhotechne.detection_system.telemetry.domain.TelemetryThresholdState;
import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetryMetric;
import com.coelhotechne.detection_system.telemetry.event.TelemetryThresholdEvent.*;
import com.coelhotechne.detection_system.telemetry.infrastructure.TelemetryThresholdStateRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.UUID;

@Log4j2
@Component
public class TelemetryThresholdBandStore {

    private final TelemetryThresholdStateRepository repository;
    private final TransactionTemplate requiresNew;
    private final Clock clock;


    public TelemetryThresholdBandStore(TelemetryThresholdStateRepository repository,
                                       PlatformTransactionManager transactionManager,
                                       Clock clock){
        this.repository = repository;
        this.clock = clock;
        this.requiresNew = new TransactionTemplate(transactionManager);
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public Band get(UUID sensorId, TelemetryMetric metric){
        return repository.findBand(TelemetryThresholdState.key(sensorId,metric)).orElse(null);
    }

    public void put(UUID sensorId,TelemetryMetric metric,Band band){
        String key = TelemetryThresholdState.key(sensorId,metric);
        try {
            Integer updated = requiresNew.execute(s-> repository.updateBand(key, band,clock.instant()));
            if (updated!=null && updated>0){
                return;
            }
            try {
                requiresNew.executeWithoutResult(s-> repository.insertBand(key,sensorId,metric.name(),band.name(),clock.instant()));
            }catch (DataIntegrityViolationException ex){
                requiresNew.executeWithoutResult(s->repository.updateBand(key,band,clock.instant()));
            }
        }catch (RuntimeException ex){
            log.warn("Could not persist threshold band {} for {}: {}",band,key,ex.getMessage());
        }
    }
}
