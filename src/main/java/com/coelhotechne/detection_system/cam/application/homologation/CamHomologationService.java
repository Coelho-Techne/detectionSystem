package com.coelhotechne.detection_system.cam.application.homologation;

import com.coelhotechne.detection_system.cam.domain.homologation.CamHomologationRecord;

import java.util.UUID;

public interface CamHomologationService {
    // Dispara o teste técnico de conexão (RTSP/NATIVE) e atualiza o registro de homologação.
    CamHomologationRecord runConnectionTest(UUID camId);
    CamHomologationRecord approve(UUID camId, String approvedBy);
    CamHomologationRecord reject(UUID camId, String rejectedBy, String reason);
}
