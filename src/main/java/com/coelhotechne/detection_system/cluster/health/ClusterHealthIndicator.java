package com.coelhotechne.detection_system.cluster.health;

import com.coelhotechne.detection_system.cluster.application.ClusterCoordinator;
import com.coelhotechne.detection_system.cluster.domain.ServerClusterNode;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("cluster")
public class ClusterHealthIndicator implements HealthIndicator {
    private final ClusterCoordinator coordinator;
    public ClusterHealthIndicator(ClusterCoordinator coordinator){
        this.coordinator = coordinator;
    }
    @Override
    public @Nullable Health health() {
        if (!coordinator.isEnabled()){
            return Health.up().withDetail("mode","single-instance").build();
        }
        if (coordinator.isHeartbeatStale()){
            return Health.down().withDetail("node_id",coordinator.nodeId())
                    .withDetail("last_heartbeat",String.valueOf(coordinator.lastSuccessfulHeartbeat()))
                    .build();
        }
        List<String>nodes = coordinator.activeNodes().stream().map(ServerClusterNode::getNodeId).toList();

        return Health.up()
                .withDetail("node_id",coordinator.nodeId())
                .withDetail("active_nodes",nodes)
                .withDetail("size",nodes.size())
                .build();
    }
}
