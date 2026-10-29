package com.coelhotechne.detection_system.device.api.controller;


import com.coelhotechne.detection_system.device.api.dto.DeviceSubscriptionRequest;
import com.coelhotechne.detection_system.device.api.dto.DeviceSubscriptionResponse;
import com.coelhotechne.detection_system.device.application.DeviceSubscriptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Device subscriptions")
@RequestMapping(value = "/api/v1/device/{deviceId}/subscriptions",produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DeviceSubscriptionController {
    private final DeviceSubscriptionService subscriptionService;

    @PreAuthorize((DeviceAccess.READ))
    @GetMapping
    public ResponseEntity<List<DeviceSubscriptionResponse>>list(@PathVariable UUID deviceId){
        return ResponseEntity.ok(subscriptionService.list(deviceId));
    }

    @PreAuthorize(DeviceAccess.WRITE_A_T_H)
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeviceSubscriptionResponse>upsert(@PathVariable UUID deviceId,
                                                            @Valid @RequestBody DeviceSubscriptionRequest request){
        return ResponseEntity.ok(subscriptionService.upsert(deviceId,request));
    }

    @PreAuthorize(DeviceAccess.WRITE_A_T_H)
    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<Void>delete(@PathVariable UUID deviceId,@PathVariable UUID subscriptionId){
        subscriptionService.delete(deviceId,subscriptionId);
        return ResponseEntity.noContent().build();
    }
}
