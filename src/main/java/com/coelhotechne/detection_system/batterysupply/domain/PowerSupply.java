package com.coelhotechne.detection_system.batterysupply.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.hibernate.annotations.OptimisticLock;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class PowerSupply {

    private static final BigDecimal MIN_PERCENTAGE = BigDecimal.ZERO;
    private static final BigDecimal MAX_PERCENTAGE = new BigDecimal("100");

    @Setter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Column(name = "power_type", nullable = false, length = 20)
    private PowerSupplyType type;
    @OptimisticLock(excluded = true)
    @Enumerated(EnumType.STRING)
    @Column(name = "power_status", nullable = false, length = 20)
    private PowerSupplyStatus status;
    @OptimisticLock(excluded = true)
    @Column(name = "power_percentage")
    private BigDecimal percentage;
    @Setter(AccessLevel.NONE)
    @Column(name = "power_rechargeable")
    private Boolean rechargeable;
    @OptimisticLock(excluded = true)
    @Column(name = "power_last_reading_at")
    private Instant lastReadingAt;
    @JsonCreator
    public PowerSupply(@JsonProperty("type") PowerSupplyType type,@JsonProperty("rechargeable") Boolean rechargeable){
        this.type=type;
        this.rechargeable=rechargeable;
        this.status=isBatteryless()
                ? PowerSupplyStatus.NOT_APPLICABLE
                : PowerSupplyStatus.NORMAL;
    }

    public record PowerOutcome(
            PowerSupplyStatus previousStatus,
            PowerSupplyStatus currentStatus,
            boolean persist
    ) {
        public boolean statusChanged() {
            return previousStatus != currentStatus;
        }
        public static PowerOutcome unchanged(PowerSupplyStatus current) {
            return new PowerOutcome(current, current, false);
        }
    }
    public boolean isBatteryless(){
        return type==PowerSupplyType.WIRED
                || type==PowerSupplyType.POWER_GRID
                || type==PowerSupplyType.NONE;
    }

    public PowerOutcome applyReading(BigDecimal newPercentage, Boolean charging, Instant observedAt,PowerSupplyThresholds thresholds){
        PowerSupplyStatus previus =this.status;
        if (type==null){
            return PowerOutcome.unchanged(previus);
        }
        if (lastReadingAt!=null && observedAt.isBefore(lastReadingAt)){
            return PowerOutcome.unchanged(previus);
        }

        BigDecimal reading = sanitize(newPercentage);
        PowerSupplyStatus resolved= resolveStatus(reading,charging,thresholds);
        boolean statusChanged = resolved!=previus;
        if (!statusChanged && !isHeartbeatDue(observedAt,thresholds.heartbeatWriteInterval())){
            return PowerOutcome.unchanged(previus);
        }
        this.lastReadingAt = observedAt;
        if (isBatteryless()){
            this.percentage=null;
        }else if (reading!=null){
            this.percentage=reading;
        }
        this.status=resolved;
        return new PowerOutcome(previus,resolved,true);
    }

    public PowerOutcome markDisconnected(){
        PowerSupplyStatus previus = this.status;
        if(isBatteryless()||previus == PowerSupplyStatus.DISCONNECTED){
            return PowerOutcome.unchanged(previus);
        }
        this.status=PowerSupplyStatus.DISCONNECTED;
        return new PowerOutcome(previus,PowerSupplyStatus.DISCONNECTED,true);
    }

    private PowerSupplyStatus resolveStatus(BigDecimal reading, Boolean charging,PowerSupplyThresholds thresholds ){
        if (isBatteryless()){
            return PowerSupplyStatus.NOT_APPLICABLE;
        }
        if (Boolean.TRUE.equals(charging)){
            return PowerSupplyStatus.CHARGING;
        }
        BigDecimal effective = reading != null ? reading : this.percentage;

        if (effective==null){
            return previousOrNormal();
        }
        if (effective.compareTo(thresholds.criticalThreshold())<=0){
            return PowerSupplyStatus.CRITICAL;
        }
        if (effective.compareTo(thresholds.lowThreshold()) <= 0){
            return PowerSupplyStatus.LOW;
        }
        return PowerSupplyStatus.NORMAL;
    }
    private PowerSupplyStatus previousOrNormal() {
        return this.status != null ? this.status : PowerSupplyStatus.NORMAL;
    }

    private static BigDecimal sanitize(BigDecimal value){
        if (value==null || value.compareTo(MIN_PERCENTAGE)<0||value.compareTo(MAX_PERCENTAGE)>0){
            return null;
        }
        return value;
    }
    private boolean isHeartbeatDue(Instant observedAt, Duration heartbeatWriteInterval){
        return lastReadingAt== null || Duration.between(lastReadingAt,observedAt).compareTo(heartbeatWriteInterval)>=0;
    }
}
