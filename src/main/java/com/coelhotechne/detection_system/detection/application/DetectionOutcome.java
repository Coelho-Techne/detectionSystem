package com.coelhotechne.detection_system.detection.application;

public enum DetectionOutcome {
    STORED,
    DUPLICATE,
    TRACK_DEDUPLICATED,
    BELOW_THRESHOLD,
    TYPE_NOT_ALLOWED,
    CONFLICT_RECORDED,
    CORRUPTED_DATA,
    NETWORK_INTRUDER,
    SYS_INTRUDER,
    DATA_INTRUDER,
    INVALID;

    public String tag() {
        return name().toLowerCase();
    }
}