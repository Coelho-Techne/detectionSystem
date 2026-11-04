package com.coelhotechne.detection_system.sensor.api.dto;

import com.coelhotechne.detection_system.installation.api.dto.InstallationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import static com.coelhotechne.detection_system.sensor.api.dto.SensorConstraints.NAME_MESSAGE;
import static com.coelhotechne.detection_system.sensor.api.dto.SensorConstraints.NAME_PATTERN;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SensorReplaceRequest(
        @NotBlank
        @Pattern(regexp = NAME_PATTERN, message = NAME_MESSAGE)
        String name,
        @NotBlank
        @Size(max = 255)
        String dataDescription,
        @NotNull
        UUID zoneUUID,
        @NotNull
        @Valid
        InstallationRequest installation
) {
}
