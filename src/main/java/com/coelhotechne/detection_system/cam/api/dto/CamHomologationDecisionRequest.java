package com.coelhotechne.detection_system.cam.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CamHomologationDecisionRequest(
        @NotBlank
        @Size(max = 500)
        String reason
) {
}
