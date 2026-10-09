package com.coelhotechne.detection_system.detection.infrastructure;

import com.coelhotechne.detection_system.cam.domain.fixed.CamFixed;
import com.coelhotechne.detection_system.detection.domain.Detection;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DetectionRepository extends JpaRepository<Detection, UUID> {
    Optional<Detection >findById(UUID uuid);
    Optional<Sensor>findDetectionBySensor(UUID uuid);
    Optional<CamFixed>findDetectionByFixedCam(UUID uuid);
    boolean existsBySourceEventId(UUID uuid);
}
