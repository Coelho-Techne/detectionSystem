package com.coelhotechne.detection_system.cam.api.controller.auth;

import com.coelhotechne.detection_system.cam.api.dto.auth.CamAuthRequest;
import com.coelhotechne.detection_system.cam.api.dto.auth.CamAuthResponse;
import com.coelhotechne.detection_system.cam.application.auth.CamAuthService;
import com.coelhotechne.detection_system.cam.application.auth.enums.CamAuthMessage;
import com.coelhotechne.detection_system.globalClass.security.IssuedAccessKeyResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Cam auth")
@RequestMapping("/api/v1/cam")
@RequiredArgsConstructor
public class CamAuthController {

    private final CamAuthService camAuthService;

    @PostMapping("/{camId}/auth")
    public ResponseEntity<CamAuthResponse> authenticate(@PathVariable UUID camId,
                                                        @Valid @RequestBody CamAuthRequest request) {
        CamAuthResponse response = camAuthService.authenticate(camId, request.accessKey());
        HttpStatus status = response.message() == CamAuthMessage.AUTORIZADO ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TECHNICIAN')")
    @PostMapping("/{camId}/access-key")
    public ResponseEntity<IssuedAccessKeyResponse>issuedAccessKey(@PathVariable UUID camId){
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.CACHE_CONTROL,"no-store")
                .body(camAuthService.issueAccessKey(camId));
    }
}
