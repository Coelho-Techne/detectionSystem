package com.coelhotechne.detection_system.cam.application.homologation;

import com.coelhotechne.detection_system.cam.api.dto.CamConnectionRequest;
import com.coelhotechne.detection_system.cam.domain.homologation.CamHomologationRecord;

import java.util.UUID;

public interface CamHomologationService {
    CamHomologationRecord getHomologation(UUID camId);
    CamHomologationRecord configureConnection(UUID camId, CamConnectionRequest request);
    // Dispara o teste técnico de conexão (RTSP/NATIVE) e atualiza o registro de homologação.
    CamHomologationRecord runConnectionTest(UUID camId);
    CamHomologationRecord approve(UUID camId, String approvedBy);
    CamHomologationRecord reject(UUID camId, String rejectedBy, String reason);
    CamHomologationRecord reopen(UUID camId,String reopenedBy);
}
