package com.coelhotechne.detection_system.detection.application;

import com.coelhotechne.detection_system.detection.api.dto.DetectionRequest;
import com.coelhotechne.detection_system.detection.api.dto.DetectionResponse;
import com.coelhotechne.detection_system.detection.domain.Detection;
import com.coelhotechne.detection_system.detection.infrastructure.DetectionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.query.Page;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.List;
import java.util.UUID;


@Service
@Log4j2
@AllArgsConstructor
public class DetectionServiceImpl implements DetectionService{

    private final DetectionRepository repository;

    @Override
    public List<Detection> findDetectionList() {
        return List.of();
    }

    @Override
    public List<Page> findDetectionPage(Pageable pageable) {
        return List.of();
    }

    @Override
    public DetectionResponse creteDetection(DetectionRequest request) {
        return null;
    }

    @Override
    public DetectionResponse updateDetection(UUID uuid, DetectionRequest request) {
        return null;
    }

    @Override
    public DetectionResponse deleteDetection(UUID uuid) {
        return null;
    }
}
