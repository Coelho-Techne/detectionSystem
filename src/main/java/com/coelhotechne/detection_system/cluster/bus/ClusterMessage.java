package com.coelhotechne.detection_system.cluster.bus;

import com.coelhotechne.detection_system.alert.notification.AlertNotification;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public record ClusterMessage(
        Type type,
        String origin,
        AlertNotification alert,
        List<UUID>deviceIds,
        List<String>principals
) {
    public enum Type{
        ALERT_BROADCAST,
        ALERT_DELIVER,
        SESSIONS_REVOKED,
        MESSAGE_FIELD,
        MESSAGE_LOST
    }
    public ClusterMessage{
        deviceIds = deviceIds == null ? List.of(): List.copyOf(deviceIds);
        principals = principals == null ? List.of(): List.copyOf(principals);
    }
}
