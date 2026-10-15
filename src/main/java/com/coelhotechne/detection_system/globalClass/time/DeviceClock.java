package com.coelhotechne.detection_system.globalClass.time;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class DeviceClock {
    public static final Duration MAX_FUTURE_SKEW = Duration.ofMinutes(5);
    public static final Duration MAX_PAST_AGE = Duration.ofHours(24);

    private DeviceClock() {
    }

    public static Instant resolve(Instant reported, Instant receivedAt) {
        Objects.requireNonNull(receivedAt, "receivedAt is required");
        if (reported == null
                || reported.isAfter(receivedAt.plus(MAX_FUTURE_SKEW))
                || reported.isBefore(receivedAt.minus(MAX_PAST_AGE))) {
            return receivedAt;
        }
        return reported;
    }
}
