package com.coelhotechne.detection_system.alert.application;

import com.coelhotechne.detection_system.alert.domain.AlertDecision;
import com.coelhotechne.detection_system.alert.domain.enums.AlertCategory;
import com.coelhotechne.detection_system.alert.domain.enums.AlertSourceType;
import com.coelhotechne.detection_system.alert.domain.enums.AlertType;
import com.coelhotechne.detection_system.alert.domain.enums.Severity;
import com.coelhotechne.detection_system.batterysupply.domain.PowerSupplyStatus;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import com.coelhotechne.detection_system.detection.domain.enums.GasHazard;
import com.coelhotechne.detection_system.detection.event.DetectionRecordedEvent;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorNiche;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorStatus;
import com.coelhotechne.detection_system.sensor.event.SensorPowerSupplyEvent;
import com.coelhotechne.detection_system.sensor.event.SensorStatusEvent;
import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetryMetric;
import com.coelhotechne.detection_system.telemetry.event.TelemetryThresholdEvent;
import com.coelhotechne.detection_system.zone.domain.enums.ZoneType;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public final class AlertRules {

    private static final Set<DetectionType>LIFE_SAFETY= EnumSet.of(
            DetectionType.FIRE, DetectionType.SMOKE, DetectionType.GAS_LEAK,
            DetectionType.CARBON_MONOXIDE, DetectionType.FALL
    );
    private static final Set<DetectionType>WATER = EnumSet.of(
            DetectionType.FLOOD,DetectionType.WATER_LEAK);
    private static final Set<DetectionType>INTRUSION = EnumSet.of(
            DetectionType.GLASS_BREAK, DetectionType.DOOR_OPEN,DetectionType.WINDOW_OPEN,
            DetectionType.PERSON,DetectionType.MOTION,DetectionType.PRESENCE
    );

    private static final Set<ZoneType> SENSITIVE_ZONES = EnumSet.of(
            ZoneType.PERIMETER, ZoneType.RESTRICTED,ZoneType.ENTRANCE);

    private static final Set<SensorNiche>LIFE_SAFETY_NICHES = EnumSet.of(SensorNiche.FIRE,SensorNiche.GAS);

    private AlertRules(){

    }

    public static AlertRuleOutcome forDetection(DetectionRecordedEvent event,
                                                ZoneType zoneType,
                                                boolean armed){

        AlertCategory category;

        AlertType type;

        Severity severity = null;

        if (LIFE_SAFETY.contains(event.type())){
            category = AlertCategory.DETECTION;
            type = AlertType.DETECTED;
            severity = Severity.CRITICAL;
        }else if (WATER.contains(event.type())){
            category = AlertCategory.DETECTION;
            type = AlertType.DETECTED;
            severity = Severity.HIGH;
        }else if (INTRUSION.contains(event.type())){
            category = AlertCategory.SECURITY;
            type = AlertType.DETECTED;
            if (!armed){
                severity = Severity.LOW;
            } else if (zoneType!=null && SENSITIVE_ZONES.contains(zoneType)) {
                severity = Severity.HIGH;
            }
            else {
                severity = Severity.MEDIUM;
            }
        } else if (event.type() == DetectionType.TAMPERING) {
            category = AlertCategory.SECURITY;
            type = AlertType.TAMPERING_DETECTED;
            severity = Severity.HIGH;
        } else if (event.type()==DetectionType.UNKNOWN) {
            category = AlertCategory.DETECTION;
            type = AlertType.ANOMALY_DETECTED;
            severity=Severity.INFO;
        }else {
            category = AlertCategory.DETECTION;
            type = AlertType.DETECTED;
            severity = Severity.MEDIUM;
        }
    if (!event.sourceHomologated()){
        severity = Severity.INFO;
    }

        AlertSourceType sourceType = AlertSourceType.of(event.deviceType());
    String key =
            event.deviceType() == DetectionDeviceType.CAMERA && event.trackId() !=null
                    ? AlertDecision.key(sourceType,event.sourceId(),
            type,event.type(),"track",event.trackId()): AlertDecision.key(
                    sourceType,event.sourceId(),type,event.type());

    return AlertRuleOutcome.raise(new AlertDecision(
            category,
            type,
            severity,
            sourceType,
            event.sourceId(),
            event.zoneId(),
            event.detectionId(),
            "%s detected".formatted(event.type().getLabel()),
            event.sourceHomologated() ? null : "Source device is not homologated",
            key,
            event.detectedAt()
    ));
    }

    public static AlertRuleOutcome forSensorStatus(SensorStatusEvent event,
                                                   SensorNiche niche,
                                                   UUID zoneId){
        String stateChanged = prefix(event.sensorId(),AlertType.STATE_CHANGED);
        String connectionLost = prefix(event.sensorId(),AlertType.CONNECTION_LOST);
        SensorStatus status = event.newStatus();
        if (status == SensorStatus.OK){
            return AlertRuleOutcome.clear(stateChanged,connectionLost);
        }

        AlertRuleOutcome base = status == SensorStatus.DISCONNECTED ? AlertRuleOutcome.NONE :
                AlertRuleOutcome.clear(connectionLost);

        return switch (status){
            case FAULT -> base.andRaise(sensorDecision(event,zoneId,AlertCategory.STATE,
                    AlertType.STATE_CHANGED,
                    Severity.HIGH,
                    "Sensor hardware fault",status));
            case OUT_OF_SPECIFICATION -> base.andRaise(sensorDecision(
                    event,
                    zoneId,
                    AlertCategory.STATE,
                    AlertType.STATE_CHANGED,
                    Severity.LOW,
                    "Sensor out of specification",status));
            case DISCONNECTED -> base.andRaise(sensorDecision(
                    event,
                    zoneId,
                    AlertCategory.COMMUNICATION,
                    AlertType.CONNECTION_LOST,
                    niche !=null &&LIFE_SAFETY_NICHES.contains(niche) ?Severity.HIGH:Severity.MEDIUM,
                    "Sensor disconnected",
                    null
            ));
            default -> base;
        };
    }

    public static AlertRuleOutcome forPower(SensorPowerSupplyEvent event,UUID zoneId){
        String resourceLow = prefix(event.sensorId(),AlertType.RESOURCE_LOW);
        PowerSupplyStatus status = event.newStatus();
        if (status == PowerSupplyStatus.NORMAL || status == PowerSupplyStatus.CHARGING){
            return AlertRuleOutcome.clear(resourceLow);
        }
        if (status != PowerSupplyStatus.LOW && status != PowerSupplyStatus.CRITICAL){
            return AlertRuleOutcome.NONE;
        }
        Severity severity = status == PowerSupplyStatus.CRITICAL ? Severity.HIGH : Severity.LOW;
        return AlertRuleOutcome.clear(resourceLow).andRaise(new AlertDecision(
                AlertCategory.RESOURCE,
                AlertType.RESOURCE_LOW,
                severity,
                AlertSourceType.SENSOR,
                event.sensorId(),
                zoneId,
                null,
                "Battery"+status.name().toLowerCase(),
                null,
                AlertDecision.key(AlertSourceType.SENSOR,event.sensorId(),AlertType.RESOURCE_LOW,status),
                event.occurredAt()
        ));
    }

    public static AlertRuleOutcome forThreshold(TelemetryThresholdEvent event){
        String exceeded = prefix(event.sensorId(),
                AlertType.THRESHOLD_EXCEEDED)+":" + event.metric();
        String below = prefix(event.sensorId(),AlertType.THRESHOLD_BELOW)+":"+event.metric();

        AlertRuleOutcome clearBoth = AlertRuleOutcome.clear(exceeded,below);

        if (event.band()==TelemetryThresholdEvent.Band.NORMAL){
            return clearBoth;
        }
        AlertType type = event.band() == TelemetryThresholdEvent.Band.ABOVE ? AlertType.THRESHOLD_EXCEEDED : AlertType.THRESHOLD_BELOW;
        return clearBoth.andRaise(new AlertDecision(
                AlertCategory.STATE,
                type,thresholdSeverity(event.metric()),
                AlertSourceType.SENSOR,
                event.sensorId(),
                event.zoneId(),
                null,
                "%s %s limit".formatted(event.metric(), type ==
                        AlertType.THRESHOLD_EXCEEDED ? "above":"below"),
                "value %s %s, limit %s".formatted(event.value().stripTrailingZeros().toPlainString(),
                        event.metric().getUnit(),event.limit() == null ? "-": event.limit().toPlainString()),
                AlertDecision.key(AlertSourceType.SENSOR,
                        event.sensorId(),
                        type,
                        event.metric()),
                event.measuredAt()));
    }

    private static Severity thresholdSeverity(TelemetryMetric metric){
        return switch (metric){
            case CO2, GAS_CONCENTRATION -> Severity.HIGH;
            case BATTERY_PERCENT, MEMORY_USED,DATA_TRANSFER,RSSI -> Severity.LOW;
            default -> Severity.MEDIUM;
        };
    }

    private static AlertDecision sensorDecision(
            SensorStatusEvent event,
            UUID zoneId,
            AlertCategory category,
            AlertType type,
            Severity severity,
            String title,
            SensorStatus sensorStatus
    ){
        String key = sensorStatus == null ? AlertDecision.key(AlertSourceType.SENSOR,event.sensorId(),type)
                : AlertDecision.key(AlertSourceType.SENSOR,event.sensorId(),type,sensorStatus);
        return new AlertDecision(category,
                type,
                severity,
                AlertSourceType.SENSOR,
                event.sensorId(),
                zoneId,
                null,
                title,"%s -> %s".formatted(event.previousStatus(),event.newStatus()),
                key,
                event.occurredAt());
    }
    private static String prefix(UUID sensorId, AlertType type){
        return AlertDecision.key(AlertSourceType.SENSOR,sensorId,type);
    }

}
