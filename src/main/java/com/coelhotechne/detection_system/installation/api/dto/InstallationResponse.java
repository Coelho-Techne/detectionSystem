package com.coelhotechne.detection_system.installation.api.dto;

import com.coelhotechne.detection_system.installation.domain.Installation;
import com.coelhotechne.detection_system.installation.domain.enums.InstallationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record InstallationResponse (
        UUID locationId,
        @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime installedAt,
        String installedBy,
        InstallationStatus status,
        String installationNotes
){
    public static InstallationResponse from(Installation installation){
        if (installation==null){
            return null;
        }
        return new InstallationResponse(
                installation.getLocation() ==null ? null:installation.getLocation().getUuid(),
                installation.getInstalledAt(),
                installation.getInstalledBy(),
                installation.getInstallationStatus(),
                installation.getInstallationNotes()
        );
    }
}
