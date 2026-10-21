package com.coelhotechne.detection_system.alert.domain.enums;

import java.util.EnumSet;
import java.util.Set;

public enum AlertStatus {
    TRIGGERED,
    ACKNOWLEDGED,
    RESOLVED,
    CLEARED;

    public static  final Set<AlertStatus>OPEN = EnumSet.of(TRIGGERED,ACKNOWLEDGED);

    public boolean isOpen(){
        return OPEN.contains(this);
    }
}
