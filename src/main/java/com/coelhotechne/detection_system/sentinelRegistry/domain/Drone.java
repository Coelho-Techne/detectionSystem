package com.coelhotechne.detection_system.sentinelRegistry.domain;

import com.coelhotechne.detection_system.batterysupply.domain.PowerSupply;
import com.coelhotechne.detection_system.device.domain.Device;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneControlType;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DronePurpose;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;


@Entity
@Getter
@Setter
@DynamicUpdate
@NoArgsConstructor
@Table(name = "drone", indexes = @Index(name = "idx_drone_controller", columnList = "controller_node_uuid"))
@PrimaryKeyJoinColumn(name = "uuid")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Drone extends Device {

    public static final String DEVICE_TYPE = "DRONE";
    public static final int NAME_MAX =  60;
    public static final int SERIAL_MAX = 100;

    @Setter(AccessLevel.NONE)
    @Column(name = "drone_name", length = NAME_MAX)
    private String droneName;

    @Column(name = "manufacturer", length = NAME_MAX)
    private String manufacturer;

    @Column(name = "model", length = NAME_MAX)
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
    private PowerSupply powerSupply;

    @Setter(AccessLevel.NONE)
    @Column(name = "controller_node_uuid")
    private UUID controllerNodeId;

    @Setter(AccessLevel.NONE)
    @Column(name = "coupled_at")
    private Instant coupledAt;


    public void coupleTo(UUID controllerNodeId, Instant now){
        Objects.requireNonNull(controllerNodeId,"controllerNodeId is required");
        if (!controllerNodeId.equals(this.controllerNodeId)){
            this.controllerNodeId = controllerNodeId;
            this.coupledAt=Objects.requireNonNull(now,"now is required");
        }
    }

    public void decouple(){
        this.controllerNodeId = null;
        this.coupledAt=null;
    }

    public boolean isCoupledTo(UUID nodeId ){
        return nodeId!=null&& nodeId.equals(controllerNodeId);
    }

    public boolean isCoupled(){
        return controllerNodeId!=null;
    }

    public void assignSerialNumber(String serialNumber){
        if (this.serialNumber !=null){
            throw new IllegalStateException("serial_number cannot be changed after registration");
        }
        this.serialNumber = normalizeSerial(serialNumber);
    }

    public void rename(String droneName){
        String name = Objects.requireNonNull(droneName,"droneName is required").trim();

        if (name.isEmpty()||name.length()>NAME_MAX){
            throw new IllegalArgumentException("droneName must have 1..."+NAME_MAX+" characters");
        }
        this.droneName = name;
        setDeviceName(name);
        setDeviceType(DEVICE_TYPE);
    }

    public boolean isActive(){
        return Boolean.TRUE.equals(getStatus());
    }

    public static String normalizeSerial(String serialNumber){
        String serial = Objects.requireNonNull(serialNumber,"serialNumber is required").trim().toUpperCase(Locale.ROOT);

        if (serial.isEmpty()|| serial.length()>SERIAL_MAX){
            throw new IllegalArgumentException("serialNumber must have 1.."+SERIAL_MAX+" characters");
        }
        return serial;
    }


}
