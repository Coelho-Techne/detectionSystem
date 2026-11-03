package com.coelhotechne.detection_system.cam.application.fixed;

import com.coelhotechne.detection_system.cam.api.dto.fixed.CamFixedRequest;
import com.coelhotechne.detection_system.cam.api.dto.fixed.CamFixedResponse;
import com.coelhotechne.detection_system.cam.domain.fixed.CamFixed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CamFixedService {
    CamFixed requireCam(UUID camId);
    List<CamFixedResponse>findCamList();
    Page<CamFixedResponse> findCamListPage(Pageable pageable);
    CamFixedResponse findCamId(UUID uuid);
    CamFixedResponse createCam(CamFixedRequest camFixedRequest);
    CamFixedResponse updateCam(UUID uuid, CamFixedRequest camFixedRequest);
    void deleteCam(UUID uuid);
}
