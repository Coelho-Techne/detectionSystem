package com.coelhotechne.detection_system.sensor.api.controller;


import com.coelhotechne.detection_system.sensor.api.dto.SensorMapper;
import com.coelhotechne.detection_system.sensor.api.dto.homologation.SensorHomologationResponse;
import com.coelhotechne.detection_system.sensor.application.homologation.SensorHomologationService;
import com.coelhotechne.detection_system.sensor.api.dto.homologation.SensorHomologationDecisionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sensor/{sensorId/homologation")
@RequiredArgsConstructor
public class SensorHomologationController {

    private final SensorHomologationService homologationService;
    private final SensorMapper mapper;

    @PreAuthorize(SensorAccess.TEST)
    @GetMapping
    public ResponseEntity<SensorHomologationResponse> get(@PathVariable UUID sensorId) {
        return ResponseEntity.ok(mapper.toHomologationResponse(homologationService.getHomologation(sensorId)));
    }

    @PostMapping("/test")
    @PreAuthorize(SensorAccess.TEST)
    public ResponseEntity<SensorHomologationResponse> requestSelfTest(@PathVariable UUID sensorId) {
        return ResponseEntity.accepted().body(mapper
                .toHomologationResponse(homologationService.requestSelfTest(sensorId)));
    }
    @PostMapping("/approve")
    @PreAuthorize(SensorAccess.DECISION)
    public ResponseEntity<SensorHomologationResponse> approve(@PathVariable UUID sensorId, Authentication authentication) {
        return ResponseEntity.ok(mapper
                .toHomologationResponse(homologationService.approve(sensorId, authentication.getName())));
    }

    @PostMapping("/reject")
    @PreAuthorize(SensorAccess.DECISION)
    public ResponseEntity<SensorHomologationResponse> reject(@PathVariable UUID sensorId,
                                                        @RequestBody @Valid SensorHomologationDecisionRequest request,
                                                        Authentication authentication) {
        return ResponseEntity.ok(mapper
                .toHomologationResponse(homologationService.reject(sensorId, authentication.getName(), request.reason())));
    }
}
