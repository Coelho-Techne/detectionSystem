package com.coelhotechne.detection_system.cam.api.controller;

import com.coelhotechne.detection_system.cam.api.dto.CamConnectionRequest;
import com.coelhotechne.detection_system.cam.api.dto.CamHomologationDecisionRequest;
import com.coelhotechne.detection_system.cam.api.dto.fixed.CamHomologationSummary;
import com.coelhotechne.detection_system.cam.application.homologation.CamHomologationService;
import com.coelhotechne.detection_system.cam.domain.homologation.CamHomologationRecord;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Cam homologation")
@RequestMapping(value = "/api/v1/cam/{camId}",produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CamHomologationController {

    private final CamHomologationService service;

    @PreAuthorize(CamAccess.EDIT)
    @GetMapping("/homologation")
    public ResponseEntity<CamHomologationSummary>get(@PathVariable UUID camId){
        return ResponseEntity.ok(CamHomologationSummary.from(service.getHomologation(camId)));
    }

    @PreAuthorize(CamAccess.EDIT)
    @PostMapping("/connection")
    public ResponseEntity<CamHomologationSummary> configureConnection(@PathVariable UUID camId,
                                                                      @Valid @RequestBody CamConnectionRequest request) {
        return ResponseEntity.ok(CamHomologationSummary.from(service.configureConnection(camId,request)));
    }
    @PreAuthorize(CamAccess.EDIT)
    @PostMapping("/homologation/test")
    public ResponseEntity<CamHomologationSummary> runConnectionTest(@PathVariable UUID camId) {
        return ResponseEntity.ok(CamHomologationSummary.from(service.runConnectionTest(camId)));
    }


    @PreAuthorize(CamAccess.ONLY_ADM)
    @PostMapping("/homologation/approve")
    public ResponseEntity<CamHomologationSummary> approve(@PathVariable UUID camId,
                                                         Authentication authentication) {
        return ResponseEntity.ok(CamHomologationSummary
                .from(service.approve(camId, authentication.getName())));
    }


    @PreAuthorize(CamAccess.ONLY_ADM)
    @PostMapping("/homologation/reject")
    public ResponseEntity<CamHomologationSummary> reject(@PathVariable UUID camId,
                                                        @Valid @RequestBody
                                                        CamHomologationDecisionRequest request,
                                                        Authentication authentication) {
        return ResponseEntity.ok(CamHomologationSummary
                .from(service.reject(camId, authentication.getName(), request.reason())));
    }
    @PreAuthorize(CamAccess.ONLY_ADM)
    @PostMapping("/homologation/reopen")
    public ResponseEntity<CamHomologationSummary>reopen(@PathVariable UUID camId,Authentication authentication){
        return ResponseEntity.ok(CamHomologationSummary.from(service.reopen(camId,authentication.getName())));
    }
}
