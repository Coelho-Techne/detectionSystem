package com.coelhotechne.detection_system.cam.application.fixed;

import com.coelhotechne.detection_system.cam.api.dto.fixed.CamFixedMapper;
import com.coelhotechne.detection_system.cam.api.dto.fixed.CamFixedRequest;
import com.coelhotechne.detection_system.cam.api.dto.fixed.CamFixedResponse;
import com.coelhotechne.detection_system.cam.domain.fixed.CamFixed;
import com.coelhotechne.detection_system.cam.exceptions.CamNotFoundException;
import com.coelhotechne.detection_system.cam.exceptions.CamConcurrentModificationException;
import com.coelhotechne.detection_system.cam.infrastructure.CamFixedRepository;
import com.coelhotechne.detection_system.installation.application.InstallationResolver;
import com.coelhotechne.detection_system.location.application.LocationService;
import com.coelhotechne.detection_system.location.domain.Location;
import com.coelhotechne.detection_system.zone.application.ZoneService;
import com.coelhotechne.detection_system.zone.domain.Zone;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
@Log4j2
public class CamFixedServiceImpl implements CamFixedService{

    private final CamFixedRepository repository;
    private final CamFixedMapper mapper;
    private final ZoneService zoneService;
    private final LocationService locationService;
    private final InstallationResolver resolver;
    private final Clock clock;

    @Override
    public CamFixed requireCam(UUID camId) {
        Objects.requireNonNull(camId,"Cam id cannot be null");
        return repository.findById(camId)
                .orElseThrow(() -> new CamNotFoundException(camId.toString(),"Cam not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CamFixedResponse> findCamList() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }
    @Override
    @Transactional(readOnly = true)
    public Page<CamFixedResponse> findCamListPage(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CamFixedResponse findCamId(UUID uuid) {
        return mapper.toResponse(requireCam(uuid));
    }

    @Override
    @Transactional
    public CamFixedResponse createCam(CamFixedRequest camFixedRequest) {
       CamFixed camFixed = new CamFixed();
       apply(camFixed, camFixedRequest);
       if (camFixed.getBaselineFrameUrl()!=null){
           camFixed.setBaselineCapturedAt(clock.instant());
       }
        return mapper.toResponse(repository.save(camFixed));
    }

    @Override
    @Transactional
    public CamFixedResponse updateCam(UUID uuid, CamFixedRequest camFixedRequest) {
        CamFixed camFixed = requireCam(uuid);
        String previousBaseLineFrameUrl = camFixed.getBaselineFrameUrl();
        if (camFixed.getBaselineFrameUrl() != null
                && !Objects.equals(previousBaseLineFrameUrl,camFixed.getBaselineFrameUrl())){
            camFixed.setBaselineCapturedAt(clock.instant());
        }
        return mapper.toResponse(saveOrConflict(camFixed));
    }

    @Override
    @Transactional
    public void deleteCam(UUID uuid) {
        repository.delete(requireCam(uuid));
        log.info("Cam with ID {}, deleted successfully",uuid);
    }

    private void apply(CamFixed camFixed,CamFixedRequest request){
        Zone zone = request.zoneId() != null ? zoneService.requireZone(request.zoneId()) : null;
        Location coverage = request.coverageLocationId() != null ? locationService.requireLocation(request.coverageLocationId()):null;
        mapper.apply(camFixed,request,zone,coverage,resolver.resolve(request.installation()));
    }
    private CamFixed saveOrConflict(CamFixed camFixed) {
        try {
            return repository.save(camFixed);
        } catch (OptimisticLockingFailureException e) {
            throw new CamConcurrentModificationException(
                    camFixed.getUuid() != null ? camFixed.getUuid().toString() : null,
                    "Cam was modified concurrently."
            );
        }
    }
}
