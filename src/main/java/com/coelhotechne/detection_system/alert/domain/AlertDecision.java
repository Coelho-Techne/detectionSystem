package com.coelhotechne.detection_system.alert.domain;

import com.coelhotechne.detection_system.alert.domain.enums.AlertCategory;
import com.coelhotechne.detection_system.alert.domain.enums.AlertSourceType;
import com.coelhotechne.detection_system.alert.domain.enums.AlertType;
import com.coelhotechne.detection_system.alert.domain.enums.Severity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AlertDecision(
        AlertCategory category,
        AlertType type,
        Severity severity,
        AlertSourceType sourceType,
        UUID sourceId,
        UUID zoneId,
        String title,
        String description,
        String dedupKey,
        Instant occurredAt
) {

    public AlertDecision {
        Objects.requireNonNull(category, "category is required");
        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(severity, "severity is required");
        Objects.requireNonNull(sourceType, "sourceType is required");
        Objects.requireNonNull(occurredAt, "occurredAt is required");
        title = clip(Objects.requireNonNull(title, "title is required"), Alert.TITLE_MAX);
        description = description == null ? null : clip(description, Alert.TEXT_MAX);
        if (dedupKey == null||dedupKey.isBlank()||dedupKey.length()>Alert.DEDUP_KEY_MAX){
            throw new IllegalArgumentException("dedup key must have 1..."+Alert.DEDUP_KEY_MAX+" char");
        }
    }

    public static String key(AlertSourceType sourceType,UUID sourceId,AlertType type,Object... qualifiers){
        StringBuilder sb = new StringBuilder().append(sourceType).append(':')
                .append(sourceId).append(':')
                .append(type);
        for (Object q : qualifiers){
            sb.append(':').append(q);
        }
        return sb.toString();
    }
    private static String clip(String value, int max){
        return value.length() <= max ? value : value.substring(0,max);
    }

}
