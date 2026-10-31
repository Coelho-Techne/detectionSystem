package com.coelhotechne.detection_system.cam.api.dto.fixed;

import com.coelhotechne.detection_system.cam.api.dto.BaseCamData;
import com.coelhotechne.detection_system.cam.domain.fixed.CamFixed;
import com.coelhotechne.detection_system.installation.api.dto.InstallationResponse;
import com.coelhotechne.detection_system.installation.domain.Installation;
import com.coelhotechne.detection_system.location.domain.Location;
import com.coelhotechne.detection_system.zone.domain.Zone;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CamFixedMapper {

    public void apply(CamFixed camFixed,
                      CamFixedRequest camFixedRequest,
                      Zone zone,
                      Location covarageLocation,
                      Installation installation) {
        Objects.requireNonNull(camFixedRequest,"Request cannot be null");
        BaseCamData.apply(camFixed,camFixedRequest,zone,installation);
        camFixed.setCoverageLocation(covarageLocation);

        camFixed.setMountingAzimuthDeg(camFixedRequest.mountingAzimuthDeg());
        camFixed.setMountingElevationDeg(camFixedRequest.mountingElevationDeg());
        camFixed.setHorizontalFovDeg(camFixedRequest.horizontalFovDeg());
        camFixed.setVerticalFovDeg(camFixedRequest.verticalFovDeg());
        camFixed.setDetectionRangeMeters(camFixedRequest.detectionRangeMeters());
        camFixed.setDetectionMaskJson(camFixedRequest.detectionMaskJson());
        camFixed.setHomographyMatrixJson(camFixedRequest.homographyMatrixJson());
        camFixed.setBaselineFrameUrl(camFixedRequest.baselineFrameUrl());
        camFixed.setCrossingLineJson(camFixedRequest.crossingLineJson());
        camFixed.setDetectionConfidenceThreshold(camFixedRequest.detectionConfidenceThreshold());
    }

    public CamFixedResponse toResponse(CamFixed entity) {
        return new CamFixedResponse(
                // BaseEntity
                entity.getUuid(),
                entity.getVersion(),
                entity.getCreatedBy(),
                entity.getLastModifiedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                // BaseCam
                entity.getCamModel(),
                entity.getFirmwareVersion(),
                entity.getSerialNumber(),
                entity.getMacAddress(),
                entity.getIpAddress(),
                entity.getRtsp(),
                entity.getCamStatus(),
                entity.getWidth(),
                entity.getHeight(),
                entity.getImageQuality(),
                entity.getFps(),
                entity.getBitrate(),
                entity.getCompression(),
                entity.getFrameUrl(),
                entity.getFrameTimestamp(),
                entity.getTimestamp(),
                entity.getPowerSupply(),
                InstallationResponse.from(entity.getInstallation()),
                entity.getZone() != null ? entity.getZone().getUuid() : null,
                CamConnectionSummary.from(entity.getCamConnection()),
                entity.getAccessKey() != null,
                entity.getLastCommunication(),
                CamHomologationSummary.from(entity.getHomologation()),
                // CamFixed
                entity.getCoverageLocation() != null ? entity.getCoverageLocation().getUuid() : null,
                entity.getMountingAzimuthDeg(),
                entity.getMountingElevationDeg(),
                entity.getHorizontalFovDeg(),
                entity.getVerticalFovDeg(),
                entity.getDetectionRangeMeters(),
                entity.getDetectionMaskJson(),
                entity.getHomographyMatrixJson(),
                entity.getBaselineFrameUrl(),
                entity.getBaselineCapturedAt(),
                entity.getCrossingLineJson(),
                entity.getDetectionConfidenceThreshold()
        );
    }
}