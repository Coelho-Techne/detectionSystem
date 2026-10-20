package com.coelhotechne.detection_system.telemetry.application;


import com.coelhotechne.detection_system.telemetry.event.TelemetryThresholdEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TelemetryThresholdEvaluatorTest {
    public static final TelemetryThresholdProperties.Limit TEMP =
            new TelemetryThresholdProperties.Limit(
                    null,
                    new BigDecimal("45"),
                    new BigDecimal("1")
            );
    public static final TelemetryThresholdProperties.Limit PH =
            new TelemetryThresholdProperties.Limit(
                    new BigDecimal("7.0"),
                    new BigDecimal("7.8"),
                    new BigDecimal("0.1")
            );
    @Test
    void crossesAbove(){
        assertThat(TelemetryThresholdEvaluator.classify(
                new BigDecimal("45.1"),
                TEMP, TelemetryThresholdEvent.Band.NORMAL
        )).isEqualTo(TelemetryThresholdEvent.Band.ABOVE);
        assertThat(TelemetryThresholdEvaluator.classify(
                new BigDecimal("45"),
                TEMP, TelemetryThresholdEvent.Band.NORMAL
        )).isEqualTo(TelemetryThresholdEvent.Band.NORMAL);
    }
    @Test
    void hysteresisHoldsAboveUntilBelowMaxMinusMargin(){
        assertThat(TelemetryThresholdEvaluator.classify(
                new BigDecimal("44.5"),
                TEMP, TelemetryThresholdEvent.Band.ABOVE
        )).isEqualTo(TelemetryThresholdEvent.Band.ABOVE);
        assertThat(TelemetryThresholdEvaluator.classify(
                new BigDecimal("44.0"),
                TEMP, TelemetryThresholdEvent.Band.ABOVE
        )).isEqualTo(TelemetryThresholdEvent.Band.NORMAL);
    }
    @Test
    void belowWithHysteresis(){
        assertThat(TelemetryThresholdEvaluator.classify(
                new BigDecimal("6.9"),
                PH, TelemetryThresholdEvent.Band.NORMAL
        )).isEqualTo(TelemetryThresholdEvent.Band.BELOW);
        assertThat(TelemetryThresholdEvaluator.classify(
                new BigDecimal("7.05"),
                PH, TelemetryThresholdEvent.Band.BELOW
        )).isEqualTo(TelemetryThresholdEvent.Band.BELOW);
        assertThat(TelemetryThresholdEvaluator.classify(
                new BigDecimal("7.2"),
                PH, TelemetryThresholdEvent.Band.BELOW
        )).isEqualTo(TelemetryThresholdEvent.Band.NORMAL);
    }
    @Test
    void firstReadingAfterRestartHasNoPreviousBand(){
        assertThat(TelemetryThresholdEvaluator.classify(
                new BigDecimal("44.5"),
                TEMP, null
        )).isEqualTo(TelemetryThresholdEvent.Band.NORMAL);
    }

}
