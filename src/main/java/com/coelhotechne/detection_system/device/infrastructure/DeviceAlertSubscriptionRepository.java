package com.coelhotechne.detection_system.device.infrastructure;

import com.coelhotechne.detection_system.alert.domain.enums.Severity;
import com.coelhotechne.detection_system.device.domain.Device;
import com.coelhotechne.detection_system.device.domain.DeviceAlertSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DeviceAlertSubscriptionRepository extends JpaRepository<DeviceAlertSubscription, UUID> {

    @Query("""
            select distinct s.device from DeviceAlertSubscription s
            where s.active = true
              and s.device.status = true
              and (s.zone is null or s.zone.uuid = :zoneId)
              and s.minSeverity in :acceptedMinSeverities
            """)
    List<Device>findRecipients(@Param("zoneId")
                               UUID zoneId,
                               @Param("acceptedMinSeverities")
                               Collection<Severity> acceptedMinSeverities);
}
