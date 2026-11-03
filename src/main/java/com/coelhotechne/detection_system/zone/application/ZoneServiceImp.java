package com.coelhotechne.detection_system.zone.application;

import com.coelhotechne.detection_system.cam.infrastructure.CamRepository;
import com.coelhotechne.detection_system.location.infrastructure.LocationRepository;
import com.coelhotechne.detection_system.sensor.infrastructure.SensorRepository;
import com.coelhotechne.detection_system.zone.api.dto.ZoneMapper;
import com.coelhotechne.detection_system.zone.api.dto.ZoneRequest;
import com.coelhotechne.detection_system.zone.api.dto.ZoneResponse;
import com.coelhotechne.detection_system.zone.domain.Zone;
import com.coelhotechne.detection_system.zone.exceptions.ZoneInUseException;
import com.coelhotechne.detection_system.zone.exceptions.ZoneNotFoundException;
import com.coelhotechne.detection_system.zone.infrastructure.ZoneRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Log4j2
@AllArgsConstructor
public class ZoneServiceImp implements ZoneService{

    private final ZoneRepository repository;
    private final ZoneMapper mapper;
    private final SensorRepository sensorRepository;
    private final CamRepository camRepository;
    private final LocationRepository locationRepository;
    private final Clock clock;

    @Override
    public Zone requireZone(UUID zoneId) {
        if (zoneId==null){
            throw new NullPointerException("Zone cannot be null");
        }
        return repository.findById(zoneId)
                .orElseThrow(() -> new ZoneNotFoundException(zoneId.toString(),"Zone not found."));
    }


    @Override
    @Transactional(readOnly = true)
    public List<ZoneResponse> findZoneList() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ZoneResponse findZoneId(UUID uuid) {
        return mapper.toResponse(requireZone(uuid));
    }

    @Override
    @Transactional
    public ZoneResponse createZone(ZoneRequest zoneRequest) {
        Zone entity = new Zone();
        mapper.toEntity(entity,zoneRequest);
        entity.setCreatedZoneAtDay(LocalDate.now());
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public ZoneResponse updateZone(UUID uuid, ZoneRequest zoneRequest) {
        Zone updated =  requireZone(uuid);
        mapper.toEntity(updated,zoneRequest);
        return mapper.toResponse(repository.saveAndFlush(updated));
    }

    @Override
    public void deleteZone(UUID uuid) {
        Zone zone = requireZone(uuid);
        long inUse = sensorRepository.countByZoneUuid(uuid)
                + camRepository.coutByZoneUuid(uuid)
                +locationRepository.countByZoneUuid(uuid);
        if (inUse > 0) {
            throw new ZoneInUseException(uuid.toString(), inUse);
        }
        repository.delete(zone);
    }
}
