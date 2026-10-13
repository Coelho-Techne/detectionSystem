package com.coelhotechne.detection_system.detection.application;

import com.coelhotechne.detection_system.detection.api.dto.DetectionDetailResponse;
import com.coelhotechne.detection_system.detection.api.dto.DetectionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DetectionQueryService {
    Page<DetectionResponse> search(DetectionFilter filter, Pageable pageable);

    /** @throws com.coelhotechne.detection_system.detection.exceptions.DetectionNotFoundException */
    DetectionDetailResponse get(UUID id);
}
