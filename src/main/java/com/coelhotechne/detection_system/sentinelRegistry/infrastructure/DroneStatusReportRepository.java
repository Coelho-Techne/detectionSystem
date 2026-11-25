package com.coelhotechne.detection_system.sentinelRegistry.infrastructure;

import com.coelhotechne.detection_system.sentinelRegistry.domain.DroneStatusReport;
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

public interface DroneStatusReportRepository extends JpaRepository<DroneStatusReport, UUID> {
    List<UUID>findExistingReportsIds(@Param("droneId")UUID droneId, @Param("ids")Collection<UUID>ids);

    @Query("""
            select r from DroneStatusReport r
             where r.droneId = :droneId and r.recordedAt >= :from and r.recordedAt < :to
            """)
    Page<DroneStatusReport>findWindow(@Param("droneId") UUID droneId,
                                      @Param("from") Instant from,
                                      @Param("to") Instant to,
                                      Pageable pageable);

    @Modifying
    @Query("delete from DroneStatusReport r where r.droneId = :droneId")
    int deleteByDrone(@Param("droneId")UUID droneId);

    @Modifying
    @Query("delete from DroneStatusReport r where r.recordedAt < :threshold")
    int deleteOlderThan(@Param("threshold")Instant threshold);

}
