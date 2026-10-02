package com.coelhotechne.detection_system.sensor.api.dto.homologation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SensorHomologationDecisionRequest (
        @NotBlank
        @Size(max = 500)
        String reason
){
}
