package com.coelhotechne.detection_system.cam.application.homologation;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CamHomologationServiceImpl implements CamHomologationService {
    private final CamRepository camRepository;
    // Porta de domínio — a implementação real (SocketRtspHandshakeClient) é injetada pelo
    // Spring a partir de cam.infrastructure.rtsp; este serviço não conhece RTSP diretamente.
    private final CamConnectionTestProbe connectionTestProbe;

    private BaseCam findOrThrow(UUID camId) {
        return camRepository.findById(camId).orElseThrow(() -> new CamNotFoundException(camId.toString(),"Id not found!"));
    }

    @Override
    @Transactional
    public CamHomologationRecord runConnectionTest(UUID camId) {
        BaseCam cam = findOrThrow(camId);
        CamConnectionProfile connection = cam.getCamConnection();
        if (connection == null) {
            throw new CamHomologationNotEligibleException(camId,
                    "Camera has no configured connection (configureCamConnection was never called)");
        }
        if (connection.getProtocol() != CamProtocol.RTSP && connection.getProtocol() != CamProtocol.NATIVE) {
            throw new CamHomologationNotEligibleException(camId,
                    "Automated testing today covers only the RTSP/NATIVE protocol." + connection.getProtocol()
                            + "Requires a dedicated probe (ONVIF/manufacturer SDK), not yet implemented.");
        }

        CamConnectionTestOutcome outcome = connectionTestProbe.test(connection);
        cam.getHomologation().applyTestResult(outcome);
        return cam.getHomologation();
    }

    @Override
    @Transactional
    public CamHomologationRecord approve(UUID camId, String approvedBy) {
        BaseCam cam = findOrThrow(camId);
        try {
            cam.getHomologation().approve(approvedBy);
        } catch (IllegalStateException e) {
            throw new CamHomologationNotEligibleException(camId, e.getMessage());
        }
        return cam.getHomologation();
    }

    @Override
    @Transactional
    public CamHomologationRecord reject(UUID camId, String rejectedBy, String reason) {
        BaseCam cam = findOrThrow(camId);
        try {
            cam.getHomologation().reject(rejectedBy, reason);
        } catch (IllegalStateException e) {
            throw new CamHomologationNotEligibleException(camId, e.getMessage());
        }
        return cam.getHomologation();
    }
}
