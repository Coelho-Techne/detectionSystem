package com.coelhotechne.detection_system.sentinelRegistry.api.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record DroneCreatedResponse (
        @JsonUnwrapped
        DroneResponse droneResponse,
        String accessKey
){
}
