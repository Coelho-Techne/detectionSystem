package com.coelhotechne.detection_system.location.api.controller;

import com.coelhotechne.detection_system.location.api.dto.LocationRequest;
import com.coelhotechne.detection_system.location.api.dto.LocationResponse;
import com.coelhotechne.detection_system.location.application.LocationService;
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
@Tag(name = "Location")
@RequestMapping(value = "/api/v1/location",produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class LocationController {

    private final LocationService service;
    @PreAuthorize(LocationAccess.READ)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LocationResponse>>findLocationList(){
        return ResponseEntity.ok().body(service.findLocationList());
    }
    @PreAuthorize(LocationAccess.READ)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LocationResponse>findLocationId(@PathVariable UUID id){
        return ResponseEntity.ok().body(service.findLocationId(id));
    }
    @PreAuthorize(LocationAccess.EDIT)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LocationResponse> createLocation(@Valid @RequestBody LocationRequest locationRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createLocation(locationRequest));
    }
    @PreAuthorize(LocationAccess.EDIT)
    @PutMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LocationResponse> updateLocation(@PathVariable UUID id,
                                                           @Valid @RequestBody LocationRequest locationRequest){
        return ResponseEntity.ok().body(service.updateLocation(id,locationRequest));
    }
    @PreAuthorize(LocationAccess.ONLY_ADM)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable UUID id){
        service.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

}
