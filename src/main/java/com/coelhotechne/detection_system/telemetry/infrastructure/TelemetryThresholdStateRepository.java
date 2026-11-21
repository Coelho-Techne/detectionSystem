package com.coelhotechne.detection_system.telemetry.infrastructure;

import com.coelhotechne.detection_system.telemetry.event.TelemetryThresholdEvent.Band;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface TelemetryThresholdStateRepository {

    @Query("select s.band from TelemetryThresholdState s where s.key = :key")
    Optional<Band>findBand(@Param("key")String key);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update TelemetryThresholdState s set s.band = :band, s.updatedAt = :now where s.key = :key")
    int updateBand(@Param("key") String key, @Param("band") Band band, @Param("now") Instant now);

    @Modifying
    @Query(value = """
            insert into telemetry_threshold_state (state_key, sensor_id, metric, band, updated_at)
            values (:key, :sensorId, :metric, :band, :now)
            """, nativeQuery = true)
    int insertBand(@Param("key") String key,
                   @Param("sensorId") UUID sensorId,
                   @Param("metric") String metric,
                   @Param("band") String band,
                   @Param("now") Instant now);

}
