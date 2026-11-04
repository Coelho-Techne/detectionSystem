package com.coelhotechne.detection_system.sensor.domain.payload;

import com.coelhotechne.detection_system.sensor.domain.homologation.SensorSelfTestOutcome;
import com.coelhotechne.detection_system.sensor.domain.homologation.enums.SensorSelfTestFailure;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SensorSelfTestPayload(
        @JsonProperty("request_id")
        UUID requestId,
        @JsonProperty("passed")
        Boolean passed,
        @JsonProperty("firmware_version")
        String firmwareVersion,
        @JsonProperty("error")
        String error,
        @JsonProperty("checks")
        Map<String,Boolean>checks
) {
    public SensorSelfTestPayload{
        checks = checks == null ? Map.of() : Map.copyOf(checks);
    }

    public boolean hasRequestId(){
        return requestId!=null;
    }

    public SensorSelfTestOutcome toOutcome(){
        if (passed == null){
            return SensorSelfTestOutcome.failed(SensorSelfTestFailure.INVALID_RESPONSE,
                    "self-test response without 'passed'");
        }if (passed){
            return SensorSelfTestOutcome.passed(summary());
        }
        String detail = error != null &&!error.isBlank() ? error : "failed checks: " + failedChecks();
        return SensorSelfTestOutcome.failed(SensorSelfTestFailure.DEVICE_REPORTED,detail);
    }

    private String summary(){
        String checksText = checks.isEmpty() ? "": "checks= "+new TreeMap<>(checks);
        return (firmwareVersion == null ? "firmware=?" : "firmware="+firmwareVersion)+checksText;
    }

    private String failedChecks(){
        String failed = new TreeMap<>(checks)
                .entrySet()
                .stream()
                .filter(e-> !Boolean.TRUE.equals(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(", "));
        return failed.isEmpty() ? "(not informed)" : failed;
    }
}
