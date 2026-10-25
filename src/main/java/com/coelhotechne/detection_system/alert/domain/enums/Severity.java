package com.coelhotechne.detection_system.alert.domain.enums;

import java.util.Arrays;
import java.util.List;

public enum Severity {
    INFO,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public boolean isAtLeast(Severity other){
        return compareTo(other)>=0;
    }

    public List<Severity>andBelow(){
        return Arrays.asList(values()).subList(0, ordinal() +1);
    }
}
