package com.coelhotechne.detection_system.telemetry.application;

import com.coelhotechne.detection_system.cluster.application.ClusterLockService;
import com.coelhotechne.detection_system.telemetry.infrastructure.TelemetryRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
@Log4j2
public class TelemetryRetentionJob {
    private final Clock clock;
    private final int batchSize;
    private final Duration retention;
    private final TransactionTemplate template;
    private final TelemetryRepository repository;
    private final ClusterLockService lockService;


    public TelemetryRetentionJob(
            TelemetryRepository repository,
            TransactionTemplate template,
            Clock clock,
            @Value("${telemetry.retention.period:90d}")
            Duration retention,
            @Value("${telemetry.retention.batch-size:5000}")
            int batchSize,
            ClusterLockService lockService
    ){
        if (batchSize <1){
            throw new IllegalArgumentException("telemetry.retention.batch-size must be >= 1");
        }
        this.repository=repository;
        this.template = template;
        this.clock=clock;
        this.retention = retention;
        this.batchSize = batchSize;
        this.lockService = lockService;
    }
    @Scheduled(cron = "${telemetry.retention.cron:0 30 3 * * *}")
    public void purge(){
        lockService.runExclusively("telemetry-retention",Duration.ofHours(1),Duration.ofMinutes(10),this::purge);
    }
    void purgeNow(){
        if (retention.isZero() || retention.isNegative()){
            return;
        }
        Instant cutoff = clock.instant().minus(retention);
        long total = 0;
        int deleted;
        do {
            Integer batch = template.execute(status -> repository.deleteOlderThan(cutoff,batchSize));
            deleted = batch == null ? 0 : batch;
            total+=deleted;
        }while (deleted == batchSize);
        if (total>0){
            log.info("Telemetry retention: {} reading(s) older than {} deleted",total,cutoff);
        }
    }
}
