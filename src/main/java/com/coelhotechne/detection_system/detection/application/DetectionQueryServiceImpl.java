package com.coelhotechne.detection_system.detection.application;

import com.coelhotechne.detection_system.detection.api.dto.DetectionDetailResponse;
import com.coelhotechne.detection_system.detection.api.dto.DetectionMapper;
import com.coelhotechne.detection_system.detection.api.dto.DetectionResponse;
import com.coelhotechne.detection_system.detection.exceptions.DetectionNotFoundException;
import com.coelhotechne.detection_system.detection.infrastructure.DetectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DetectionQueryServiceImpl implements DetectionQueryService{
    private final DetectionRepository repository;
    private final DetectionMapper mapper;
    private final Clock clock;
    @Override
    public Page<DetectionResponse> search(DetectionFilter filter, Pageable pageable) {
        DetectionFilter bounded = filter.withDefaults(clock.instant());
        return repository.findAll(DetectionSpecifications.of(bounded),pageable).map(mapper::toResponse);
    }

    @Override
    public DetectionDetailResponse get(UUID id) {
        return repository.findById(id).map(mapper::toDetail).orElseThrow(()->new DetectionNotFoundException(id));
    }
}
