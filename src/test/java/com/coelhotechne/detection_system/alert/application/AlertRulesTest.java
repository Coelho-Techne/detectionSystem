package com.coelhotechne.detection_system.alert.application;

import com.coelhotechne.detection_system.alert.domain.AlertDecision;
import com.coelhotechne.detection_system.alert.domain.enums.*;
import com.coelhotechne.detection_system.batterysupply.domain.PowerSupplyStatus;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import com.coelhotechne.detection_system.detection.event.DetectionRecordedEvent;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorNiche;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorStatus;
import com.coelhotechne.detection_system.sensor.event.SensorPowerSupplyEvent;
import com.coelhotechne.detection_system.sensor.event.SensorStatusEvent;
import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetryMetric;
import com.coelhotechne.detection_system.telemetry.event.TelemetryThresholdEvent;
import com.coelhotechne.detection_system.zone.domain.enums.ZoneType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AlertRulesTest {

    private static final Instant T0 = Instant.parse("2026-10-27T17:00:00Z");
    private static final UUID SOURCE = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ZONE = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static DetectionRecordedEvent detection(DetectionType type, DetectionDeviceType deviceType,
                                                    Long trackId, boolean homologated){
        return new DetectionRecordedEvent(
                UUID.randomUUID(),
                deviceType,
                SOURCE,type,ZONE,
                T0,trackId,homologated
        );
    }

    @ParameterizedTest
    @EnumSource(
            value = DetectionType.class,
            names = {"FIRE","SMOKE","GAS_LEAK","CARBON_MONOXIDE","FALL"}
    )
    void lifeSafetyIsCritical(DetectionType type){
        AlertDecision d = AlertRules.forDetection(
                detection(type,DetectionDeviceType.SENSOR, null,true), ZoneType.INTERNAL,true
        ).raise();

        assertThat(d.severity()).isEqualTo(Severity.CRITICAL);
        assertThat(d.category()).isEqualTo(AlertCategory.DETECTION);
    }

    @Test
    void intrusionDependsOnZoneAndArming(){
        DetectionRecordedEvent door = detection(DetectionType.DOOR_OPEN,
                DetectionDeviceType.SENSOR,null,true);
        assertThat(AlertRules.forDetection(door,ZoneType.ENTRANCE,true).raise().severity()).isEqualTo(Severity.HIGH);
        assertThat(AlertRules.forDetection(door,ZoneType.ENTRANCE,false).raise().severity()).isEqualTo(Severity.LOW);
        assertThat(AlertRules.forDetection(door,ZoneType.INTERNAL,true).raise().severity()).isEqualTo(Severity.MEDIUM);
        assertThat(AlertRules.forDetection(door,null,true).raise().severity()).isEqualTo(Severity.MEDIUM);
    }

    @Test
    void unknownIsInfoAnomaly(){
        AlertDecision d = AlertRules.forDetection(detection(
                DetectionType.UNKNOWN,
                DetectionDeviceType.SENSOR,
                null,
                true),null,true).raise();

        assertThat(d.type()).isEqualTo(AlertType.ANOMALY_DETECTED);
        assertThat(d.severity()).isEqualTo(Severity.INFO);
    }
    @Test
    void nonHomologatedSourceIsDowngraded(){
        AlertDecision d =AlertRules.forDetection(
                detection(DetectionType.MOTION,DetectionDeviceType.SENSOR,null,false),null,true
        ).raise();
        assertThat(d.severity()).isEqualTo(Severity.INFO);
    }
    @Test
    void nonHomologatedLifeSafetyStaysHigh(){
        AlertDecision decision = AlertRules.forDetection(
                detection(
                        DetectionType.FIRE,
                        DetectionDeviceType.SENSOR,
                        null,
                        false
                ),null,true
        ).raise();
        assertThat(decision.severity()).isEqualTo(Severity.HIGH);
    }
    @Test
    void cameraKeyIsPerTrack(){
        String a = AlertRules.forDetection(
                detection(DetectionType.PERSON,DetectionDeviceType.CAMERA,1L,true),
                null,true).raise().dedupKey();
        String b = AlertRules.forDetection(
                detection(DetectionType.PERSON,DetectionDeviceType.CAMERA,2L,true),
                null,true).raise().dedupKey();
        assertThat(a).isNotEqualTo(b).startsWith("CAMERA:");
    }

    @Test
    void sameSensorSameTypeSameKey(){
        String a = AlertRules.forDetection(
                detection(DetectionType.MOTION,DetectionDeviceType.SENSOR,
                        null,true),
                null,true).raise().dedupKey();
        String b = AlertRules.forDetection(
                detection(DetectionType.MOTION,DetectionDeviceType.SENSOR,
                        null,true),
                null,true).raise().dedupKey();
        assertThat(a).isEqualTo(b);
    }
    @Test
    void disconnectedFireSensorIsHigh(){
        SensorStatusEvent e = new SensorStatusEvent(
                UUID.randomUUID(),
                SOURCE,
                SensorStatus.OK,
                SensorStatus.DISCONNECTED,
                T0);
        AlertRuleOutcome fire = AlertRules.forSensorStatus(e, SensorNiche.FIRE,ZONE);
        AlertRuleOutcome presence = AlertRules.forSensorStatus(e,SensorNiche.PRESENCE,ZONE);

        assertThat(fire.raise().type()).isEqualTo(AlertType.CONNECTION_LOST);
        assertThat(fire.raise().severity()).isEqualTo(Severity.HIGH);
        assertThat(presence.raise().severity()).isEqualTo(Severity.MEDIUM);
    }

    @Test
    void backToOkClearsStateAndConnectionAlerts(){
        SensorStatusEvent e =new SensorStatusEvent(
                UUID.randomUUID(),SOURCE,SensorStatus.DISCONNECTED,SensorStatus.OK,T0
        );
        AlertRuleOutcome outcome = AlertRules.forSensorStatus(e,SensorNiche.FIRE,ZONE);
        assertThat(outcome.raise()).isNull();
        assertThat(outcome.clearPrefixes()).containsExactlyInAnyOrder(
                "SENSOR:" + SOURCE + ":STATE_CHANGED", "SENSOR:" + SOURCE + ":CONNECTION_LOST");
    }
    //configurado com equalTo devido a falta do metodo containsExactly
    @Test
    void batteryCriticalReplacesLow(){
        SensorPowerSupplyEvent e = new SensorPowerSupplyEvent(
                UUID.randomUUID(),
                SOURCE, PowerSupplyStatus.LOW,PowerSupplyStatus.CRITICAL,T0
        );

        AlertRuleOutcome outcome = AlertRules.forPower(e,ZONE);
        assertThat(outcome.clearPrefixes()).containsExactly(
                "SENSOR:" + SOURCE + ":RESOURCE_LOW");
        assertThat(outcome.raise().severity()).isEqualTo(Severity.HIGH);
    }
    @Test
    void batteryChargingOnlyClears(){
        SensorPowerSupplyEvent e = new SensorPowerSupplyEvent(UUID.randomUUID(),
                SOURCE, PowerSupplyStatus.CRITICAL,PowerSupplyStatus.CHARGING,T0);
       assertThat(AlertRules.forPower(e,ZONE).raise()).isNull();
    }
    @Test
    void thresholdAboveAndBackToNormal(){
        TelemetryThresholdEvent above = new TelemetryThresholdEvent(UUID.randomUUID(),
                SOURCE,ZONE,
                TelemetryMetric.CO2,new BigDecimal("1800"),new BigDecimal("1500"),
                TelemetryThresholdEvent.Band.NORMAL,TelemetryThresholdEvent.Band.ABOVE,T0);
        AlertDecision d = AlertRules.forThreshold(above).raise();
        assertThat(d.type()).isEqualTo(AlertType.THRESHOLD_EXCEEDED);
        assertThat(d.severity()).isEqualTo(Severity.HIGH);

        TelemetryThresholdEvent normal = new TelemetryThresholdEvent(
                UUID.randomUUID(),SOURCE,ZONE,TelemetryMetric.CO2,new BigDecimal("1200"),null,
                TelemetryThresholdEvent.Band.ABOVE,TelemetryThresholdEvent.Band.NORMAL,T0
        );

        AlertRuleOutcome back = AlertRules.forThreshold(normal);
        assertThat(back.raise()).isNull();
        assertThat(back.clearPrefixes()).anyMatch(p -> d.dedupKey().equals(p));
    }

}
