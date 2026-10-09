package com.coelhotechne.detection_system.detection.application;

import com.coelhotechne.detection_system.detection.api.dto.DetectionRequest;
import com.coelhotechne.detection_system.detection.api.dto.DetectionResponse;
import com.coelhotechne.detection_system.detection.domain.Detection;
import org.hibernate.query.Page;

import java.awt.print.Pageable;
import java.util.List;
import java.util.UUID;

public interface DetectionService {
    List<Detection>findDetectionList();
    List<Page>findDetectionPage(Pageable pageable);
    DetectionResponse creteDetection(DetectionRequest request);
    DetectionResponse updateDetection(UUID uuid,DetectionRequest request);
    DetectionResponse deleteDetection(UUID uuid);
}
