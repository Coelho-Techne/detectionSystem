package com.coelhotechne.detection_system.alert.infrastructure;

import com.coelhotechne.detection_system.alert.domain.Alert;
import com.coelhotechne.detection_system.alert.domain.enums.AlertStatus;
import com.coelhotechne.detection_system.alert.domain.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID>, JpaSpecificationExecutor<Alert> {

    @Modifying(flushAutomatically = true)
    @Query(value = """
            insert into alert (uuid, version, created_at, updated_at,
                               category, type, severity, status, source_type, source_id,
                               detection_id, zone_id, title, description, dedup_key,
                               occurrence_count, first_occurred_at, last_occurred_at)
            values (:uuid, 0, :now, :now,
                    :category, :type, :severity, 'TRIGGERED', :sourceType, :sourceId,
                    :detectionId, :zoneId, :title, :description, :dedupKey,
                    1, :occurredAt, :occurredAt) as incoming
            on duplicate key update
                occurrence_count = alert.occurrence_count + 1,
                last_occurred_at = greatest(alert.last_occurred_at, incoming.last_occurred_at),
                updated_at       = incoming.updated_at,
                version          = alert.version + 1
            """,nativeQuery = true)
    int upsertOpen(
            @Param("uuid")
            UUID uuid,
            @Param("now")
            Instant now,
            @Param("category")
            String category,
            @Param("type")
            String type,
            @Param("severity")
            String severity,
            @Param("sourceType")
            String sourceType,
            @Param("sourceId")
            UUID sourceId,
            @Param("detectionId")
            UUID detectionId,
            @Param("zoneId")
            UUID zoneId,
            @Param("title")
            String title,
            @Param("description")
            String description,
            @Param("dedupKey")
            String dedupKey,
            @Param("occurredAt")
            Instant occurredAt
    );

    Optional<Alert>findByOpenDedupKey(String openDedupKey);
    @Query(
            """
            select a from Alert a
            where a.status in :open
              and (a.dedupKey = :prefix or a.dedupKey like concat(:prefix, ':%'))
            """
    )
    List<Alert>findOpenByKeyPrefix(
            @Param("prefix")
            String prefix,
            @Param("open")
            Collection<AlertStatus>open
    );

    boolean existsByDedupKeyAndClosedAtAfter(String dedupKey,Instant since);

    @Query(
            """
            select a.severity as severity, count(a) as total
            from Alert a
            where a.status in :open
            group by a.severity
            """
    )
    List<SeverityCount>countOpenBySeverity(
            @Param("open")
            Collection<AlertStatus>open);
    @Query(
            """
            select z.uuid as zoneId, z.name as zoneName, count(a) as total
            from Alert a left join a.zone z
            where a.status in :open
            group by z.uuid, z.name
            """
    )
    List<ZoneCount> countOpenByZone(
            @Param("open")
            Collection<AlertStatus>open
    );

    interface SeverityCount{
        Severity getSeverity();
        long getTotal();
    }
    interface ZoneCount{
        UUID getZoneId();
        Severity getZoneName();
        long getTotal();
    }
}
