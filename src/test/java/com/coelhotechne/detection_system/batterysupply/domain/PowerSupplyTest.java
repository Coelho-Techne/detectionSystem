package com.coelhotechne.detection_system.batterysupply.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PowerSupplyTest {
    private static final PowerSupplyThresholds THRESHOLDS =
            new PowerSupplyThresholds(
                    new BigDecimal("30"),new BigDecimal("15"),
                    Duration.ofMinutes(15));
    private static final Instant TO = Instant.parse("2026-10-09T12:00:00Z");

    public static PowerSupply battery(){
        return new PowerSupply(PowerSupplyType.RECHARGEABLE_BATTERY,true);
    }

    @Test
    void outcomeMatcheStoredStatus(){
        PowerSupply power = battery();

        PowerSupply.PowerOutcome outcome = power.applyReading(new BigDecimal("20"),
                false,TO,THRESHOLDS);

        assertThat(outcome.currentStatus()).isEqualTo(PowerSupplyStatus.LOW);
        assertThat(outcome.currentStatus()).isEqualTo(power.getStatus());
        assertThat(outcome.statusChanged()).isTrue();
    }

    @Test
    void criticalAndCharging(){
        PowerSupply powerSupply = battery();
        assertThat(powerSupply.applyReading(new BigDecimal("10"),
                false,TO,THRESHOLDS)).isEqualTo(PowerSupplyStatus.CRITICAL);

        assertThat(powerSupply.applyReading(new BigDecimal("10"),
                true,TO.plusSeconds(1),THRESHOLDS)).isEqualTo(
                        PowerSupplyStatus.CHARGING);
    }

    void outOfOrderReadingIsIgnored(){
        PowerSupply powerSupply = battery();
        PowerSupply.PowerOutcome late = powerSupply.applyReading(new BigDecimal("5"),
                false,TO.minusSeconds(60),THRESHOLDS);
        assertThat(late.persist()).isFalse();
        assertThat(powerSupply.getStatus()).isEqualTo(PowerSupplyStatus.NORMAL);
    }
    @Test
    void sameStatusBeforeHeartbeatIsNotPersistent(){
        PowerSupply powerSupply = battery();
        powerSupply.applyReading(new BigDecimal("80"),
                false,TO,THRESHOLDS);

        assertThat(powerSupply.applyReading(new BigDecimal("79"),
                false,TO.plusSeconds(1),THRESHOLDS).persist()).isFalse();
    }
}