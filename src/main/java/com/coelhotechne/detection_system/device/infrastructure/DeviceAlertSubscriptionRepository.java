package com.coelhotechne.detection_system.device.infrastructure;

import com.coelhotechne.detection_system.device.domain.Device;
import com.coelhotechne.detection_system.device.domain.DeviceAlertSubscription;
import com.coelhotechne.detection_system.event.domain.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DeviceAlertSubscriptionRepository extends JpaRepository<DeviceAlertSubscription, UUID> {
    List<Device>findRecipients(@Param("zoneId")
                               UUID zoneId,
                               @Param("acceptedMinSeverities")
                               Collection<Severity> acceptedMinSeverities);
}
