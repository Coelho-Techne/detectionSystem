package com.coelhotechne.detection_system.security.event;

import java.util.Set;

public record SessionRevocationEvent(Set<String> principals,String reason) {
    public SessionRevocationEvent{
        principals = Set.copyOf(principals);
    }

    public static SessionRevocationEvent of(String principal, String reason){
        return new SessionRevocationEvent(Set.of(principal),reason);
    }
}
