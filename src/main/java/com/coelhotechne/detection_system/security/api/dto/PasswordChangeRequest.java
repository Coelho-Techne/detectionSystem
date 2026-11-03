package com.coelhotechne.detection_system.security.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PasswordChangeRequest(
        String currentPassword,
        @NotBlank
        @Size(min = 10,max = 128)
        String newPassword
) {
}
