package com.coelhotechne.detection_system.zone.api.controller;

import com.coelhotechne.detection_system.zone.api.dto.ZoneRequest;
import com.coelhotechne.detection_system.zone.api.dto.ZoneResponse;
import com.coelhotechne.detection_system.zone.application.ZoneService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Zone")
@RequestMapping(value = "/api/v1/zone",produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService service;

    @PreAuthorize(ZoneAccess.READ)
    @GetMapping
    public ResponseEntity<List<ZoneResponse>>findZoneList(){
        return ResponseEntity.status(HttpStatus.OK).body(service.findZoneList());
    }
    @PreAuthorize(ZoneAccess.READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<ZoneResponse>findZoneId(@PathVariable UUID id){
        return ResponseEntity.ok(service.findZoneId(id));
    }

    @PreAuthorize(ZoneAccess.ONLY_ADM)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ZoneResponse>createZone(@Valid @RequestBody ZoneRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createZone(request));
    }
    @PreAuthorize(ZoneAccess.ONLY_ADM)
    @PutMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ZoneResponse>updateZone(@PathVariable UUID id,@Valid @RequestBody ZoneRequest request){
        return ResponseEntity.ok(service.updateZone(id,request));
    }
    @PreAuthorize(ZoneAccess.ONLY_ADM)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void>deleteZone(@PathVariable UUID id){
        service.deleteZone(id);
        return ResponseEntity.noContent().build();
    }
}
