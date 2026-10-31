package com.coelhotechne.detection_system.cam.api.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record CamAuthRequest (
        @NotBlank
        String accessKey
){
}
