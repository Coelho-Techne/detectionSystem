package com.coelhotechne.detection_system.detection.infrastructure;

import com.coelhotechne.detection_system.detection.domain.Detection;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface DetectionRepository extends JpaRepository<Detection, UUID>, JpaSpecificationExecutor<Detection> {
    boolean existsBySourceEventId(UUID sourceEventId);

    Optional<Detection> findBySourceEventId(UUID sourceEventId);

    /** Deduplicação por rastro da câmera (§4.4). Usa {@code idx_cam_detection_track}. */
    @Query("""
            select count(d) > 0 from CamDetection d
            where d.cam.uuid = :camId
              and d.trackId = :trackId
              and d.type = :type
              and d.detectedAt >= :since
            """)
    boolean existsCamTrackSince(@Param("camId") UUID camId,
                                @Param("trackId") Long trackId,
                                @Param("type") DetectionType type,
                                @Param("since") Instant since);
}
