package com.coelhotechne.detection_system.cam.api.controller;

import com.coelhotechne.detection_system.cam.api.ptz.CamPtzRequest;
import com.coelhotechne.detection_system.cam.api.ptz.CamPtzResponse;
import com.coelhotechne.detection_system.cam.api.ptz.PtzCommandRequest;
import com.coelhotechne.detection_system.cam.application.ptz.CamPtzService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@Tag(name = "CamPtz")
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/cam-ptz",produces = MediaType.APPLICATION_JSON_VALUE)
public class CamPtzController {
    private final CamPtzService service;

    @PreAuthorize(CamAccess.READ)
    @GetMapping
    public ResponseEntity<Page<CamPtzResponse>>list(Pageable pageable){
        return ResponseEntity.ok(service.list(pageable));
    }
    @PreAuthorize(CamAccess.READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<CamPtzResponse>get(@PathVariable UUID id){
        return ResponseEntity.ok(service.get(id));
    }
    @PreAuthorize(CamAccess.EDIT)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CamPtzResponse>create(@Valid @RequestBody CamPtzRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PreAuthorize(CamAccess.EDIT)
    @PutMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CamPtzResponse>update(@PathVariable UUID id, @RequestBody @Valid CamPtzRequest request){
        return ResponseEntity.ok(service.update(id,request));
    }
    @PreAuthorize(CamAccess.ONLY_ADM)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PreAuthorize(CamAccess.WRITE_A_T_H)
    @PostMapping(value = "/{id}/command",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CamPtzResponse>command(@PathVariable UUID id, @Valid @RequestBody PtzCommandRequest request){
        return ResponseEntity.accepted().body(service.command(id,request));
    }
}
