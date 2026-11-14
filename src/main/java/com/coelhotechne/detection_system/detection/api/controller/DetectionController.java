package com.coelhotechne.detection_system.detection.api.controller;


import com.coelhotechne.detection_system.detection.api.dto.DetectionDetailResponse;
import com.coelhotechne.detection_system.detection.api.dto.DetectionResponse;
import com.coelhotechne.detection_system.detection.application.DetectionFilter;
import com.coelhotechne.detection_system.detection.application.DetectionQueryService;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Detection")
@RequestMapping(value = "/api/v1/detection",produces = MediaType.APPLICATION_JSON_VALUE)
public class DetectionController {

    private final DetectionQueryService queryService;

    @PreAuthorize(DetectionAccess.READ)
    @GetMapping
    public ResponseEntity<Page<DetectionResponse>> search(
            @RequestParam(name = "zone_id", required = false) UUID zoneId,
            @RequestParam(name = "device_type", required = false) DetectionDeviceType deviceType,
            @RequestParam(name = "type", required = false) DetectionType type,
            @RequestParam(name = "source_id", required = false) UUID sourceId,
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 50,sort = "detectedAt",direction = Sort.Direction.DESC)Pageable pageable
            ){
        DetectionFilter filter = new DetectionFilter(zoneId,deviceType,type,sourceId,from,to);
        return ResponseEntity.ok(queryService.search(filter,pageable));
    }


    @PreAuthorize(DetectionAccess.READ)
    @GetMapping("/{id}")
    public ResponseEntity<DetectionDetailResponse>get(@PathVariable UUID id){
        return ResponseEntity.ok(queryService.get(id));
    }

}
