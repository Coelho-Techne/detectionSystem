package com.coelhotechne.detection_system.detection.api.controller;

import com.coelhotechne.detection_system.detection.api.dto.CamDetectionRequest;
import com.coelhotechne.detection_system.detection.api.dto.DetectionResponse;
import com.coelhotechne.detection_system.detection.application.CamDetectionIngestService;
import com.coelhotechne.detection_system.detection.application.DetectionFilter;
import com.coelhotechne.detection_system.detection.application.DetectionQueryService;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import com.coelhotechne.detection_system.detection.exceptions.DetectionSourceMismatchException;
import com.coelhotechne.detection_system.security.config.device.CamDeviceAuthenticationToken;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/cam/{camId}/detections",produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Camera detections")
public class CamDetectionController {

    private final CamDetectionIngestService ingestService;
    private final DetectionQueryService queryService;

    @PreAuthorize(DetectionAccess.EDIT_AUTH)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?>ingest(@PathVariable UUID camId,
                                   @Valid @RequestBody CamDetectionRequest request,
                                   Authentication authentication){
        if (authentication instanceof CamDeviceAuthenticationToken token
        && !camId.equals(token.getDeviceId())){
            throw new DetectionSourceMismatchException(camId,token.getDeviceId());
        }
        CamDetectionIngestService.Outcome result = ingestService.ingest(camId,request.toCommand());
        return switch (result.outcome()){
            case STORED ->  ResponseEntity
                    .created(URI.create("/api/v1/detection/"+result.detection().uuid()))
                    .body(result.detection());
            case DUPLICATE -> ResponseEntity.ok(result.detection());
            default -> ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                    "outcome",result.outcome().name(),
                    "reason",result.reason() == null ? "":result.reason()
            ));
        };
    }
    @PreAuthorize(DetectionAccess.READ)
    @GetMapping
    public ResponseEntity<Page<DetectionResponse>>search(
            @PathVariable UUID camId,
            @RequestParam(name = "type",required = false)DetectionType type,
            @RequestParam(name = "from",required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)Instant from,
            @RequestParam(name = "to",required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)Instant to,
            @PageableDefault(size = 50,sort = "detectedAt",direction = Sort.Direction.DESC)Pageable pageable
            ){
        DetectionFilter filter = new DetectionFilter(null, DetectionDeviceType.CAMERA,type,camId,from,to);
        return ResponseEntity.ok(queryService.search(filter,pageable));
    }

}
