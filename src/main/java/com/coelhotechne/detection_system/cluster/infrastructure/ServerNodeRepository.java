package com.coelhotechne.detection_system.cluster.infrastructure;

import com.coelhotechne.detection_system.cluster.domain.ServerClusterNode;
import com.coelhotechne.detection_system.cluster.domain.enums.NodeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ServerNodeRepository extends JpaRepository<ServerClusterNode, String> {
    List<ServerClusterNode>findByStatusOrderByNodeId(NodeStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ServerNode n set n.status = :down
            where n.status = :up and n.lastHeartbeat < :threshold
            """)
    int markStale(@Param("threshold") Instant threshold,
                  @Param("up") NodeStatus up,
                  @Param("down") NodeStatus down);
}
