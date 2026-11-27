package com.coelhotechne.detection_system.sentinelRegistry.application;


import com.coelhotechne.detection_system.cluster.application.ClusterLockService;
import com.coelhotechne.detection_system.sentinelRegistry.config.DroneLinkProperties;
import com.coelhotechne.detection_system.sentinelRegistry.infrastructure.DroneFlightEventRepository;
import com.coelhotechne.detection_system.sentinelRegistry.infrastructure.DroneStatusReportRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Log4j2
@Component
public class DroneLinkRetentionJob {

    private final Clock clock;
    private final TransactionTemplate template;
    private final ClusterLockService lockService;
    private final DroneLinkProperties properties;
    private final DroneFlightEventRepository eventRepository;
    private final DroneStatusReportRepository reportRepository;

    public DroneLinkRetentionJob(Clock clock,
                                 DroneLinkProperties properties,
                                 DroneStatusReportRepository reportRepository,
                                 DroneFlightEventRepository eventRepository,
                                 TransactionTemplate template,
                                 ClusterLockService lockService) {
        this.clock = clock;
        this.properties = properties;
        this.reportRepository = reportRepository;
        this.eventRepository = eventRepository;
        this.template = template;
        this.lockService = lockService;
    }
    @Scheduled(cron = "${drone.link.retention-cron:0 15 4 * * *}")
    public void purge(){
        lockService.runExclusively("drone-link-retention", Duration.ofMinutes(30),Duration.ofMinutes(1),this::purgeNow);
    }

    void purgeNow(){
        Instant now = clock.instant();
        Integer reports = template.execute(s-> reportRepository.deleteOlderThan(now.minus(properties.reportRetention())));

        Integer events = template.execute(s-> eventRepository.deleteOlderThan(now.minus(properties.eventRetention())));

        log.info("Drone link retention: {} status report(s) and {} event(s) removed",reports,events);
    }
}
