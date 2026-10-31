package com.coelhotechne.detection_system.cam.application.ptz;

import com.coelhotechne.detection_system.cam.api.ptz.CamPtzMapper;
import com.coelhotechne.detection_system.cam.api.ptz.CamPtzRequest;
import com.coelhotechne.detection_system.cam.api.ptz.CamPtzResponse;
import com.coelhotechne.detection_system.cam.api.ptz.PtzCommandRequest;
import com.coelhotechne.detection_system.cam.domain.ptz.CamPtz;
import com.coelhotechne.detection_system.cam.domain.ptz.PtzCommandSender;
import com.coelhotechne.detection_system.cam.exceptions.CamInvalidRequestException;
import com.coelhotechne.detection_system.cam.exceptions.CamNotFoundException;
import com.coelhotechne.detection_system.cam.exceptions.CamPtzCommandException;
import com.coelhotechne.detection_system.cam.infrastructure.CamPtzRepository;
import com.coelhotechne.detection_system.installation.application.InstallationResolver;
import com.coelhotechne.detection_system.zone.application.ZoneService;
import com.coelhotechne.detection_system.zone.domain.Zone;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class CamPtzService {

    private final CamPtzRepository repository;
    private final CamPtzMapper mapper;
    private final ZoneService zoneService;
    private final InstallationResolver installationResolver;
    private final PtzCommandSender commandSender;
    private final Clock clock;

    @Transactional(readOnly = true)
    public Page<CamPtzResponse>list(Pageable pageable){
        return repository.findAll(pageable).map(mapper::toResponse);
    }
    @Transactional
    public CamPtzResponse get(UUID uuid){
        return mapper.toResponse(require(uuid));
    }
    @Transactional
    public CamPtzResponse create(CamPtzRequest request){
        CamPtz camPtz = new CamPtz();
        apply(camPtz,request);
        return mapper.toResponse(repository.save(camPtz));
    }
    @Transactional
    public CamPtzResponse update(UUID id,CamPtzRequest camPtzRequest){
        CamPtz camPtz = require(id);
        apply(camPtz,camPtzRequest);
        return mapper.toResponse(repository.saveAndFlush(camPtz));
    }
    @Transactional
    public void delete(UUID id){
        repository.delete(require(id));
    }
    @Transactional
    public CamPtzResponse command(UUID id, PtzCommandRequest request){
        CamPtz camPtz = require(id);
        try {
            camPtz.applyCommand(request.action(),request.value(),clock.instant());
        }catch (IllegalArgumentException ex){
            throw new CamInvalidRequestException(id,ex.getMessage());
        }
        try {
            commandSender.send(camPtz,request.action(),request.value());
        }catch (PtzCommandSender.PtzCommandException ex){
            throw new CamPtzCommandException(id,ex);
        }
        return mapper.toResponse(camPtz);
    }

    private void  apply(CamPtz camPtz,CamPtzRequest request){
        Zone zone = request.zoneId() ==null ? null: zoneService.requireZone(request.zoneId());
        try {
            mapper.apply(camPtz,request,zone,installationResolver.resolve(request.installation()));
        }catch (IllegalArgumentException ex){
            throw new CamInvalidRequestException(camPtz.getUuid(),ex.getMessage());
        }
    }

    private CamPtz require(UUID id){
        Objects.requireNonNull(id,"id is required");
        return repository.findById(id).orElseThrow(()-> new CamNotFoundException(id.toString(),"PTZ not found!"));
    }
}
