package com.coelhotechne.detection_system.globalClass.time;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DeviceClockTest {
    private static final Instant RECEIVED = Instant.parse("2026-10-09T23:41:05Z");

    @Test
    void nullUsesReceivedAt(){
        assertThat(DeviceClock
                .resolve(null,RECEIVED))
                .isEqualTo(RECEIVED);
    }

    @Test
    void pastWithinWindowUsesDeviceTime(){
        Instant reported = RECEIVED.minusSeconds(30);
        assertThat(DeviceClock.resolve(reported,RECEIVED)).isEqualTo(reported);
    }
    @Test
    void futureWithinSkewUsesDeviceTime(){
        Instant reported = RECEIVED.plus(Duration.ofMinutes(4));
        assertThat(DeviceClock.resolve(reported,RECEIVED)).isEqualTo(reported);
    }
    @Test
    void futureBeyondSkewUsesReceivedAt(){
        Instant reported = RECEIVED.plus(DeviceClock.MAX_FUTURE_SKEW).minusSeconds(1);
        assertThat(DeviceClock.resolve(reported,RECEIVED)).isEqualTo(RECEIVED);
    }
    void resetRtcUsesReceivedAt(){
        assertThat(DeviceClock.resolve(Instant.EPOCH,RECEIVED)).isEqualTo(RECEIVED);
    }
}
