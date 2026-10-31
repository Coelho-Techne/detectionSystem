package com.coelhotechne.detection_system.cam.api.dto;

import com.coelhotechne.detection_system.cam.domain.connectioncam.CamConnectionParams;
import com.coelhotechne.detection_system.cam.domain.connectioncam.enums.CamCapability;
import com.coelhotechne.detection_system.cam.domain.connectioncam.enums.CamProtocol;
import com.coelhotechne.detection_system.connection.domain.enums.LinkType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.Set;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CamConnectionRequest(
        @NotNull
        CamProtocol protocol,
        @Size(max = 512)
        String streamUri,
        @Size(max = 255)
        String host,
        @Min(1) @Max(65535)
        Integer port,
        @Size(max = 64)
        String vendorSdkId,
        @Size(max = 100)
        String credentialRef,
        LinkType linkType,
        @Size(max = 64)
        String ipAddress,
        @Min(-150) @Max(0)
        Integer signalStrengthDbm,
        Set<CamCapability> capabilities
) {
    public CamConnectionParams toParms(){
        return new CamConnectionParams(
                streamUri,
                host,
                port,
                vendorSdkId,
                credentialRef,
                linkType,
                ipAddress,
                signalStrengthDbm,
                capabilities
        );
    }
}
