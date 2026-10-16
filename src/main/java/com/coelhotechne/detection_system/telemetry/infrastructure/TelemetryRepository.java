package com.coelhotechne.detection_system.telemetry.infrastructure;

import com.coelhotechne.detection_system.telemetry.domain.Telemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TelemetryRepository extends JpaRepository<Telemetry, UUID>, JpaSpecificationExecutor<Telemetry> {

    boolean existsBySensorUuidAndSourceMessageId(UUID sensorId,UUID sourceMessageId);

    List<Telemetry> findBySensorUuidOrderByMeasuredAtDesc(UUID sensorId);

    @Query("""
            select t from Telemetry t
            where t.sensor.uuid = :sensorId
              and t.measuredAt = (
                  select max(t2.measuredAt) from Telemetry t2
                  where t2.sensor.uuid = :sensorId and t2.metric = t.metric)
            order by t.metric
            """)
    List<Telemetry> findLatestPerMetric(
            @Param("sensorId")
            UUID sensorId);

    // consulta por janela de tempo, útil para gráficos/relatórios
    @Query(value = """
                   select floor(unix_timestamp(t.measured_at) / :bucketSeconds) * :bucketSeconds as bucketStart,
                   avg(t.metric_value) as avgValue,
                   min(t.metric_value) as minValue,
                   max(t.metric_value) as maxValue,
                   count(*)            as samples
            from telemetry t
            where t.sensor_id = :sensorId
              and t.metric = :metric
              and t.measured_at >= :from
              and t.measured_at < :to
            group by bucketStart
            order by bucketStart
        
            """,nativeQuery = true)
    List<TelemetrySeriesRow> findSeries(
            @Param("sensorId")
            UUID sensorId,
            @Param("metric")
            String metric,
            @Param("from")
            Instant from,
            @Param("to")
            Instant to,
            @Param("bucketSeconds")
            long bucketSeconds);

    @Modifying
    @Query(value = "delete from telemetry where measured_at < : cutoff limit :limit",nativeQuery = true)
    int deleteOlderThan(@Param("cutoff")Instant cutoff,@Param("limit") int limit);

    interface TelemetrySeriesRow{
        Number getBucketStart();
        Number getAvgValue();
        Number getMinValue();
        Number getMaxValue();
        Number getSamples();
    }

}
