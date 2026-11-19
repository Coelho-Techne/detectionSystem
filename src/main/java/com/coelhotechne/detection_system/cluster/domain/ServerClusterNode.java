package com.coelhotechne.detection_system.cluster.domain;


import com.coelhotechne.detection_system.cluster.domain.enums.NodeStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cluster_node",indexes = @Index(name = "idx_cluster_node_status",
        columnList = "status, last_heartbeat"))
public class ServerClusterNode {
    @Id
    @Column(name = "node_id",length = 64)
    private String nodeId;
    @Column(name = "host")
    private String host;
    @Column(name = "started_at",nullable = false)
    private Instant startedAt;
    @Column(name = "last_heartbeat",nullable = false)
    private Instant lastHeartbeat;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false,length = 10)
    private NodeStatus status;

    public ServerClusterNode(String nodeId, String host, Instant now){
        this.nodeId = nodeId;
        this.host = host;
        this.startedAt = now;
        this.lastHeartbeat = now;
        this.status = NodeStatus.UP;
    }

    public void heartbeat(Instant now){
        this.lastHeartbeat = now;
        this.status = NodeStatus.UP;
    }

    public void restart(String host, Instant now){
        this.host = host;
        this.startedAt = now;
        heartbeat(now);
    }
    public void leave(Instant now){
        this.lastHeartbeat = now;
        this.status = NodeStatus.LEFT;
    }
}
