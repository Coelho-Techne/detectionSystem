package com.coelhotechne.detection_system.sensor.infrastructure;

import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorStatus;
import com.coelhotechne.detection_system.sensor.domain.homologation.enums.SensorHomologationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorRepository extends JpaRepository<Sensor, UUID> {
    Optional<Sensor> findByName(String name);
    @EntityGraph(attributePaths = "zone")
    Optional<Sensor>findByNameAndZoneName(String sensorName, String zoneName);
    Long countByZoneUuid(UUID uuid);

    @Query("select s.uuid from Sensor s where s.name = :sensorName and s.zone.name = :zoneName")
    Optional<UUID> findUuidByNameAndZoneName(@Param("sensorName") String sensorName,
                                             @Param("zoneName") String zoneName);
    @EntityGraph(attributePaths = "zone")
    Page<Sensor> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "zone")
    Page<Sensor> findByZoneUuid(UUID zoneUuid, Pageable pageable);

    @Query("""
            select s from Sensor s
            where s.sensorStatus <> :disconnected
              and (s.lastReadingAt is null or s.lastReadingAt < :threshold)
            """)
    List<Sensor> findStaleSensors(@Param("threshold") Instant threshold,
                                  @Param("disconnected") SensorStatus disconnected);

    @Query("""
        select s.uuid from Sensor s
        where s.homologation.status = :status
          and s.homologation.testRequestedAt < :threshold
        """)
    List<UUID> findOverdueSelfTestIds(@Param("status") SensorHomologationStatus status,
                                      @Param("threshold") Instant threshold);
}