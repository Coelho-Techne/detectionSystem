package com.coelhotechne.detection_system.alert.application;

import java.time.Instant;
import java.util.UUID;

public interface ZoneArmingPolicy {
    boolean isArmed(UUID zoneId, Instant at);
}
