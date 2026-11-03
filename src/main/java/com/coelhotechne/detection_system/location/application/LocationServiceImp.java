package com.coelhotechne.detection_system.location.application;

import com.coelhotechne.detection_system.location.api.dto.LocationMapper;
import com.coelhotechne.detection_system.location.api.dto.LocationRequest;
import com.coelhotechne.detection_system.location.api.dto.LocationResponse;
import com.coelhotechne.detection_system.location.domain.Location;
import com.coelhotechne.detection_system.location.exceptions.LocationIdNotFoundException;
import com.coelhotechne.detection_system.location.infrastructure.LocationRepository;
import com.coelhotechne.detection_system.zone.application.ZoneService;
import com.coelhotechne.detection_system.zone.domain.Zone;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Log4j2
@AllArgsConstructor
public class LocationServiceImp implements LocationService{
    private final LocationRepository repository;
    private final LocationMapper mapper;
    private final ZoneService zoneService;

    @Override
    public Location requireLocation(UUID locationId) {
        if (locationId==null){
            throw new NullPointerException("Location cannot be null");
        }
        return repository.findById(locationId)
                .orElseThrow(() -> new LocationIdNotFoundException(locationId.toString(),"Location ID Not Found."));

    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> findLocationList() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponse findLocationId(UUID uuid) {
        return mapper.toResponse(requireLocation(uuid));
    }

    @Override
    @Transactional
    public LocationResponse createLocation(LocationRequest locationRequest) {
        Zone zone = zoneService.requireZone(locationRequest.zoneUuid());
        Location entity = mapper.toEntity(locationRequest);
        entity.setZone(zone);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public LocationResponse updateLocation(UUID uuid, LocationRequest locationRequest) {
        Location updated =requireLocation(uuid);
        Zone zone = zoneService.requireZone(locationRequest.zoneUuid());
        updated.setName(locationRequest.name());
        updated.setLatitude(locationRequest.latitude());
        updated.setLongitude(locationRequest.longitude());
        updated.setZone(zone);
        Location saved = repository.saveAndFlush(updated);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteLocation(UUID uuid) {
        repository.delete(requireLocation(uuid));
    }
}
