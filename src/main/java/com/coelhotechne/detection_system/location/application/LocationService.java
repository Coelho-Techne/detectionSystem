package com.coelhotechne.detection_system.location.application;

import com.coelhotechne.detection_system.location.api.dto.LocationRequest;
import com.coelhotechne.detection_system.location.api.dto.LocationResponse;
import com.coelhotechne.detection_system.location.domain.Location;

import java.util.List;
import java.util.UUID;

public interface LocationService {
    Location requireLocation(UUID locationId);
    List<LocationResponse>findLocationList();
    LocationResponse findLocationId(UUID uuid);
    LocationResponse createLocation(LocationRequest locationRequest);
    LocationResponse updateLocation(UUID uuid, LocationRequest locationRequest);
    void deleteLocation(UUID uuid);
}
