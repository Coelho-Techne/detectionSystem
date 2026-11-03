package com.coelhotechne.detection_system.security.api.dto;

import com.coelhotechne.detection_system.security.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserCreateRequest(
        @NotBlank
        @Email
        @Size(max = 255)
        String email,
        @NotBlank @Size(min = 10,max = 128)
        String password,
        @NotNull
        Role role,
        @Size(max = 255)
        String fullName
) {
}
