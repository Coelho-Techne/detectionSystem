package com.coelhotechne.detection_system.device.api.controller;

import com.coelhotechne.detection_system.device.api.dto.auth.DeviceAuthRequest;
import com.coelhotechne.detection_system.device.api.dto.auth.DeviceAuthResponse;
import com.coelhotechne.detection_system.device.application.auth.DeviceAuthService;
import com.coelhotechne.detection_system.device.domain.enums.DeviceMessage;
import com.coelhotechne.detection_system.globalClass.security.IssuedAccessKeyResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Device-auth")
@RequestMapping(value = "/api/v1/device",produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class DeviceAuthController {

    private final DeviceAuthService authService;

    @PostMapping(value = "/{id}/authenticate",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DeviceAuthResponse> authenticate(@PathVariable UUID id,
                                                           @Valid @RequestBody DeviceAuthRequest request){
       DeviceAuthResponse response = authService.authenticate(id,request.accessKey());
       HttpStatus status = response.result()
               == DeviceMessage.AUTHORIZED ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status)
                .body(response);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/{id}/rotate-key")
    public ResponseEntity<IssuedAccessKeyResponse>rotateAccessKey(@PathVariable UUID id){
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL,"no-store")
                .body(authService.rotateAccessKey(id));
    }
}
