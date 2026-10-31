package com.coelhotechne.detection_system.cam.infrastructure;

import com.coelhotechne.detection_system.cam.domain.base.BaseCam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CamRepository extends JpaRepository<BaseCam, UUID> {
    long coutByZoneUuid(UUID zoneId);
}
