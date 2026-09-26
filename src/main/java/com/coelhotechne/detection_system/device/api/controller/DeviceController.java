package com.coelhotechne.detection_system.device.api.controller;

import com.coelhotechne.detection_system.device.api.dto.DeviceRequest;
import com.coelhotechne.detection_system.device.api.dto.DeviceResponse;
import com.coelhotechne.detection_system.device.application.DeviceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/device",produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Device")
public class DeviceController {

    private final DeviceService deviceService;

    @PreAuthorize(DeviceAccess.READ)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DeviceResponse>> findDeviceList(){
        return ResponseEntity.status(HttpStatus.OK).body(deviceService.findDeviceList());
    }
    @PreAuthorize(DeviceAccess.READ)
    @GetMapping(value = "/{id}"
            ,produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeviceResponse>findDeviceId(@PathVariable UUID id){
        return ResponseEntity.status(HttpStatus.OK).body(deviceService.findDeviceId(id));
    }
    @PreAuthorize(DeviceAccess.EDIT)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE
            ,consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody DeviceRequest deviceRequest){
        DeviceResponse response = deviceService.createDevice(deviceRequest);
        URI location = URI.create("/device/"+response.uuid());
        return ResponseEntity.created(location).body(response);
    }
    @PreAuthorize(DeviceAccess.EDIT)
    @PutMapping(value = "/{id}"
            ,produces = MediaType.APPLICATION_JSON_VALUE
            ,consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeviceResponse> updateDevice(@PathVariable UUID id,@Valid @RequestBody DeviceRequest deviceRequest){
        return ResponseEntity.status(HttpStatus.OK).body(deviceService.updateDevice(id,deviceRequest));
    }

    @PreAuthorize(DeviceAccess.ONLY_ADM)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<DeviceResponse> deleteDevice(@PathVariable UUID id){
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(deviceService.deleteDevice(id));
    }
}