package com.coelhotechne.detection_system.location.infrastructure;

import com.coelhotechne.detection_system.location.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {
    Optional<Location>findById(UUID uuid);
    long countByZoneUuid(UUID zoneId);
}
