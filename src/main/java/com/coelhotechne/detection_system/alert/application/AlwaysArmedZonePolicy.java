package com.coelhotechne.detection_system.alert.application;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class AlwaysArmedZonePolicy implements ZoneArmingPolicy {
    public boolean isArmed(UUID zoneId, Instant at){
        return true;
    }
}
