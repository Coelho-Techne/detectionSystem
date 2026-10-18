package com.coelhotechne.detection_system.telemetry.application;

import com.coelhotechne.detection_system.telemetry.infrastructure.TelemetryRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
@Log4j2
public class TelemetryRetentionJob {
    private final TelemetryRepository repository;
    private final TransactionTemplate template;
    private final Clock clock;
    private final Duration retention;
    private final int bactchSize;


    public TelemetryRetentionJob(
            TelemetryRepository repository,
            TransactionTemplate template,
            Clock clock,
            Duration retention,
            int bactchSize
    ){
        this.repository=repository;
        this.template = template;
        this.clock=clock;
        this.retention = retention;
        this.bactchSize = bactchSize;
    }

    public void purge(){
        if (retention.isZero() || retention.isNegative()){
            return;
        }
        Instant cutoff = clock.instant().minus(retention);
        long total = 0;
        int deleted = 0;
        do {
            Integer batch = template.execute(status -> repository.deleteOlderThan(cutoff,bactchSize));
            total+=deleted;
        }while (deleted == bactchSize);
        if (total>0){
            log.info("Telemetry retention: {} reanding(s) older than {} deleted",total,cutoff);
        }
    }
}
