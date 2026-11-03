package com.coelhotechne.detection_system.zone.application;

import com.coelhotechne.detection_system.zone.api.dto.ZoneRequest;
import com.coelhotechne.detection_system.zone.api.dto.ZoneResponse;
import com.coelhotechne.detection_system.zone.domain.Zone;

import java.util.List;
import java.util.UUID;

public interface ZoneService {
    Zone requireZone(UUID zoneId);
    List<ZoneResponse>findZoneList();
    ZoneResponse findZoneId(UUID uuid);
    ZoneResponse createZone(ZoneRequest zoneRequest);
    ZoneResponse updateZone(UUID uuid, ZoneRequest zoneRequest);
    void deleteZone(UUID uuid);
}
