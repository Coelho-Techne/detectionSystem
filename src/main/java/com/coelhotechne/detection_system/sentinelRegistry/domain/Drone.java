package com.coelhotechne.detection_system.sentinelRegistry.domain;

import com.coelhotechne.detection_system.batterysupply.domain.PowerSupply;
import com.coelhotechne.detection_system.device.domain.Device;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneControlType;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DronePurpose;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@DynamicUpdate
@NoArgsConstructor
@Table(name = "drone")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Drone extends Device {

    @Column(name = "drone_name", length = 30)
    private String droneName;

    @Column(name = "manufacturer", length = 60)
    private String manufacturer;

    @Column(name = "model", length = 60)
    private String model;

    @Column(name = "serial_number", unique = true, length = 100)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "drone_type", length = 30)
    private DroneType droneType;

    @Enumerated(EnumType.STRING)
    @Column(name = "drone_purpose", length = 30)
    private DronePurpose dronePurpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "control_type", length = 30)
    private DroneControlType controlType;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(name = "max_payload_grams")
    private Integer maxPayloadGrams;

    @Column(name = "max_flight_time_minutes")
    private Integer maxFlightTimeMinutes;

    @Column(name = "max_speed_kmh")
    private Double maxSpeedKmh;

    @Column(name = "max_altitude_meters")
    private Double maxAltitudeMeters;

    @Column(name = "max_range_meters")
    private Double maxRangeMeters;

    @Column(name = "firmware_version", length = 30)
    private String firmwareVersion;

    @Column(name = "communication_protocol", length = 50)
    private String communicationProtocol;

    @Column(name = "manufactured_at")
    private LocalDate manufacturedAt;

    @Column(name = "last_maintenance_at")
    private LocalDateTime lastMaintenanceAt;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "status",
                    column = @Column(name = "power_supply_status", length = 20)
            ),
            @AttributeOverride(
                    name = "type",
                    column = @Column(name = "power_supply_type", length = 20)
            ),
            @AttributeOverride(
                    name = "percentage",
                    column = @Column(name = "power_supply_percentage", precision = 5, scale = 2)
            ),
            @AttributeOverride(
                    name = "lastReadingAt",
                    column = @Column(name = "power_supply_last_reading_at")
            )
    })
    @EqualsAndHashCode.Exclude
    private PowerSupply powerSupply;
}
