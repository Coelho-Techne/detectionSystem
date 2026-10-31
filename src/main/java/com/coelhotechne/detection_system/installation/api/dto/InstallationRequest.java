package com.coelhotechne.detection_system.installation.api.dto;

import com.coelhotechne.detection_system.installation.domain.Installation;
import com.coelhotechne.detection_system.installation.domain.enums.InstallationStatus;
import com.coelhotechne.detection_system.location.domain.Location;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record InstallationRequest (
        UUID locationId,
        LocalDateTime installedAt,
        @Size(max = 255)
        String installedBy,
        InstallationStatus status,
        @Size(max = 255)
        String installationNotes
){
    public Installation toEntity(Location location){
        return new Installation(
                location,
                installedAt,
                installedBy,
                status,
                installationNotes
        );
    }
}
