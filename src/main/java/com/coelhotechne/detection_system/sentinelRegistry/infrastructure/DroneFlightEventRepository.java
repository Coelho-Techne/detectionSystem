package com.coelhotechne.detection_system.sentinelRegistry.infrastructure;

import com.coelhotechne.detection_system.sentinelRegistry.domain.DroneFlightEvent;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DroneFlightEventRepository extends JpaRepository<DroneFlightEvent, UUID> {
    List<UUID>findExistingEventIds(@Param("droneId")UUID droneId, @Param("ids")Collection<UUID>ids);

    Page<DroneFlightEvent>findWindow(@Param("droneId")UUID droneId,
                                     @Param("types")Collection<DroneEventType>types,
                                     @Param("from")Instant from,
                                     @Param("to")Instant to,
                                     Pageable pageable);
    @Modifying
    @Query("delete from DroneFlightEvent e where e.droneId = :droneId")
    int deleteByDrone(@Param("droneId")UUID droneId);

    @Modifying
    @Query("delete from DroneFlightEvent e where e.occurredAt < :threshold")
    int deleteOlderThan(@Param("threshold") Instant threshold);
}
