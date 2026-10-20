package com.coelhotechne.detection_system.telemetry.domain.enums;

import com.coelhotechne.detection_system.sensor.domain.enums.SensorNiche;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TelemetryMetricTest {
    @Test
    void fromCodeIsTolerant(){
        assertThat(TelemetryMetric.fromCode("temperature")).isEqualTo(TelemetryMetric.TEMPERATURE);
        assertThat(TelemetryMetric.fromCode("Soil-Moisture")).isEqualTo(TelemetryMetric.SOIL_MOISTURE);
        assertThat(TelemetryMetric.fromCode("pm2.5")).isEqualTo(TelemetryMetric.PM2_5);
    }
    @Test
    void unknownCodeIsNull(){
        assertThat(TelemetryMetric.fromCode("flux_capacitor")).isNull();
        assertThat(TelemetryMetric.fromCode("unknown")).isNull();
        assertThat(TelemetryMetric.fromCode(null)).isNull();
    }
    @Test
    void diagnosticsAllowedForAnyNiche(){
        for (SensorNiche niche : SensorNiche.values()){
            assertThat(TelemetryMetric.BATTERY_PERCENT.allowsNiche(niche)).isTrue();
        }
    }
    @Test
    void measurementRestrictedToNiche(){
        assertThat(TelemetryMetric.PH.allowsNiche(SensorNiche.POOL)).isTrue();
        assertThat(TelemetryMetric.PH.allowsNiche(SensorNiche.SECURITY)).isFalse();
        assertThat(TelemetryMetric.UNKNOWN.allowsNiche(SensorNiche.POOL)).isFalse();
    }
}
