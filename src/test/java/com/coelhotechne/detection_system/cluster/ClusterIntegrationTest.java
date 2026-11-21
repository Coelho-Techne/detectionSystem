package com.coelhotechne.detection_system.cluster;


import com.coelhotechne.detection_system.cluster.application.ClusterCoordinator;
import com.coelhotechne.detection_system.cluster.application.ClusterLockService;
import com.coelhotechne.detection_system.cluster.config.ClusterProperties;
import com.coelhotechne.detection_system.cluster.domain.ServerClusterNode;
import com.coelhotechne.detection_system.cluster.domain.enums.NodeStatus;
import com.coelhotechne.detection_system.cluster.infrastructure.ClusterLeaseRepository;
import com.coelhotechne.detection_system.cluster.infrastructure.ServerNodeRepository;
import com.coelhotechne.detection_system.telemetry.application.TelemetryThresholdBandStore;
import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetryMetric;
import com.coelhotechne.detection_system.telemetry.event.TelemetryThresholdEvent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "app.bootstrap.admin-email=admin@cluster.local",
        "app.bootstrap.admin-password=admin-password-cluster",
        "cluster.enabled=true",
        "cluster.node-id=node-a",
        "cluster.heartbeat-interval=1h",
        "cluster.node-timeout=2h"
})
@ActiveProfiles("test")
class ClusterIntegrationTest {
    @Autowired
    ClusterLockService lockA;
    @Autowired
    ClusterLeaseRepository leases;
    @Autowired
    ServerNodeRepository nodes;
    @Autowired
    ClusterCoordinator coordinator;
    @Autowired
    PlatformTransactionManager txManger;
    @Autowired
    TelemetryThresholdBandStore bandStore;

    @Test
    void onlyOneNodeRunsAScheduledTaskWhileTheLeaseIsValid(){
        ClusterLockService lockB = nodeB(Clock.systemUTC());
        AtomicInteger runs = new AtomicInteger();
        String task ="test-task"+ UUID.randomUUID();

        boolean ranA = lockA.runExclusively(task,Duration.ofMinutes(5),Duration.ofMinutes(1),()->{
            runs.incrementAndGet();
            assertThat(lockB.runExclusively(task,Duration.ofMinutes(5),Duration.ZERO,runs::incrementAndGet)).isFalse();
        });
        assertThat(ranA).isTrue();

        assertThat(lockB.runExclusively(task,Duration.ofMinutes(5),Duration.ZERO,runs::incrementAndGet)).isFalse();

        ClusterLockService lockBLater =nodeB(Clock.offset(Clock.systemUTC(),
                Duration.ofMinutes(2)));
        assertThat(lockBLater.runExclusively(task,Duration.ofMinutes(5),Duration.ZERO,runs::incrementAndGet)).isTrue();
        assertThat(runs).hasValue(2);
        assertThat(leases.findById(task)).get().extracting(l->l.getOwner()).isEqualTo("node-b");
    }
    @Test
    void nodeRegistersItselfAndStaleNodesAreMarkedDown(){
        assertThat(nodes.findById("node-a")).get().extracting(ServerClusterNode::getStatus).isEqualTo(NodeStatus.UP);

        TransactionTemplate tx = new TransactionTemplate(txManger);
        Instant longAgo = Instant.now().minus(Duration.ofDays(1));
        tx.executeWithoutResult(s-> nodes.save(new ServerClusterNode("node-zombie","old-host",longAgo)));
        tx.executeWithoutResult(s-> nodes.markStale(Instant.now().minus(Duration.ofHours(2)),NodeStatus.UP,NodeStatus.DOWN));

        assertThat(nodes.findById("node-zombie")).get().extracting(ServerClusterNode::getStatus)
                .isEqualTo(NodeStatus.DOWN);
        assertThat(coordinator.activeNodes()).extracting(ServerClusterNode::getNodeId)
                .contains("node-a").doesNotContain("node-zombie");
    }
    @Test
    void thresholdBandIsSharedThroughTheDatabase(){
        UUID sensor = UUID.randomUUID();
        assertThat(bandStore.get(sensor, TelemetryMetric.TEMPERATURE)).isNull();
        bandStore.put(sensor,TelemetryMetric.TEMPERATURE, Band.ABOVE);
        assertThat(bandStore.get(sensor, TelemetryMetric.TEMPERATURE)).isEqualTo(Band.ABOVE);
        bandStore.put(sensor,TelemetryMetric.TEMPERATURE, Band.NORMAL);
        assertThat(bandStore.get(sensor, TelemetryMetric.TEMPERATURE)).isEqualTo(Band.NORMAL);
    }

    private ClusterLockService nodeB(Clock clock){
        ClusterProperties b = new ClusterProperties(true,"node-b", Duration.ofHours(1),
                Duration.ofHours(2),
                null,
                null);
        return new ClusterLockService(leases,b,txManger,clock.withZone(ZoneOffset.UTC));
    }
}
