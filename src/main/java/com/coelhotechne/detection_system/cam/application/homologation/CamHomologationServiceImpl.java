package com.coelhotechne.detection_system.cam.application.homologation;

import com.coelhotechne.detection_system.cam.api.dto.CamConnectionRequest;
import com.coelhotechne.detection_system.cam.domain.base.BaseCam;
import com.coelhotechne.detection_system.cam.domain.connectioncam.CamConnectionProfile;
import com.coelhotechne.detection_system.cam.domain.connectioncam.CamConnectionTestProbe;
import com.coelhotechne.detection_system.cam.domain.connectioncam.enums.CamProtocol;
import com.coelhotechne.detection_system.cam.domain.homologation.CamConnectionTestOutcome;
import com.coelhotechne.detection_system.cam.domain.homologation.CamHomologationRecord;
import com.coelhotechne.detection_system.cam.exceptions.CamHomologationNotEligibleException;
import com.coelhotechne.detection_system.cam.exceptions.CamNotFoundException;
import com.coelhotechne.detection_system.cam.infrastructure.CamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Log4j2
@Service
@RequiredArgsConstructor
public class CamHomologationServiceImpl implements CamHomologationService {
    private final CamRepository camRepository;
    // Porta de domínio — a implementação real (SocketRtspHandshakeClient) é injetada pelo
    // Spring a partir de cam.infrastructure.rtsp; este serviço não conhece RTSP diretamente.
    private final CamConnectionTestProbe connectionTestProbe;
    private final TransactionTemplate template;
    private final Clock clock;


    private BaseCam findOrThrow(UUID camId) {
        return camRepository.findById(camId).orElseThrow(() -> new CamNotFoundException(camId.toString(),"Id not found!"));
    }

    @Override
    @Transactional(readOnly = true)
    public CamHomologationRecord getHomologation(UUID camId) {
        return findOrThrow(camId).getHomologation();
    }

    @Override
    @Transactional
    public CamHomologationRecord configureConnection(UUID camId, CamConnectionRequest request) {
        BaseCam cam = findOrThrow(camId);
        try {
            cam.configureCamConnection(request.protocol(),request.toParms());
        }catch (IllegalArgumentException ex){
            throw new CamHomologationNotEligibleException(camId,ex.getMessage());
        }
        log.info("Connection of camera {} set to {}",camId,request.protocol());
        return cam.getHomologation();
    }

    @Override
    @Transactional
    public CamHomologationRecord runConnectionTest(UUID camId) {
        CamConnectionProfile connection = template.execute(status -> {
            BaseCam cam = findOrThrow(camId);
            CamConnectionProfile profile = cam.getCamConnection();
            if (profile == null) {
                throw new CamHomologationNotEligibleException(camId,
                        "Camera has no configured connection (PUT /api/v1/cam/{camId}/connection)");
            }
            if (profile.getProtocol() != CamProtocol.RTSP && profile.getProtocol() != CamProtocol.NATIVE) {
                throw new CamHomologationNotEligibleException(camId,
                        "Automated testing today covers only the RTSP/NATIVE protocol." + profile.getProtocol()
                                + "Requires a dedicated probe (ONVIF/manufacturer SDK), not yet implemented.");
            }
            guarded(camId,()->{
                cam.getHomologation().requireTestable();
                return null;
            });
            return profile;
        });

        CamConnectionTestOutcome outcome = connectionTestProbe.test(connection);
        return template.execute(status -> {
            BaseCam cam=findOrThrow(camId);
            CamConnectionProfile current=cam.getCamConnection();
            if (current==null ||
            current.getProtocol()!=connection.getProtocol()
            ||!Objects.equals(current.getStreamUri(),connection.getStreamUri())){
                throw new CamHomologationNotEligibleException(camId,
                        "Connection changed while the test was running; run the test again");
            }
            guarded(camId,()->{
                cam.getHomologation().applyTestResult(outcome,clock.instant());
                return null;
            });
            log.info("Connection test of camera {}: {}",camId,outcome.success() ? "ok":outcome.errorMessage());
            return cam.getHomologation();
        });
    }

    @Override
    @Transactional
    public CamHomologationRecord approve(UUID camId, String approvedBy) {
        BaseCam cam = findOrThrow(camId);
        guarded(camId,()->{
            cam.getHomologation().approve(approvedBy,clock.instant());
            return null;
        });
        log.info("Camera {} homologation approved by {}",camId,approvedBy);
        return cam.getHomologation();
    }

    @Override
    @Transactional
    public CamHomologationRecord reject(UUID camId, String rejectedBy, String reason) {
        BaseCam cam = findOrThrow(camId);
        guarded(camId,()->{
            cam.getHomologation().reject(rejectedBy,reason,clock.instant());
            return null;
        });
        log.info("Camera {} homologation rejected by {}",camId,rejectedBy);
        return cam.getHomologation();
    }

    @Override
    @Transactional
    public CamHomologationRecord reopen(UUID camId, String reopenedBy) {
        BaseCam cam = findOrThrow(camId);
        var previous = cam.getHomologation().getStatus();
        guarded(camId,()->{
            cam.getHomologation().reopen();
            return null;
        });
        log.info("Camera {} homologation reopened by {} (was {})",camId,reopenedBy,previous);
        return cam.getHomologation();
    }

    private static <T> T guarded(UUID camId, Supplier<T>action){
        try {
            return action.get();
        }catch (IllegalStateException ex){
            throw new CamHomologationNotEligibleException(camId,ex.getMessage());
        }
    }
}
