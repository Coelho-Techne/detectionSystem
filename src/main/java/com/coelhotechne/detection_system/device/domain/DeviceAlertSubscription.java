package com.coelhotechne.detection_system.device.domain;


import com.coelhotechne.detection_system.alert.domain.enums.Severity;
import com.coelhotechne.detection_system.globalClass.entities.BaseEntity;
import com.coelhotechne.detection_system.zone.domain.Zone;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "device_alert_subscription",
uniqueConstraints = @UniqueConstraint(
        name = "uk_device_alert_subscrition", columnNames = {"device_id","zone_id"}),
indexes = @Index(name = "idx_device_alert_subscription_zone",columnList = "zone_id, active"))
public class DeviceAlertSubscription extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "device_id",nullable = false,updatable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id",updatable = false)
    private Zone zone;

    @Enumerated(EnumType.STRING)
    @Column(name = "min_severity",nullable = false,length = 10)
    private Severity minSeverity = Severity.MEDIUM;

    @Column(name = "active",nullable = false)
    private boolean active = true;

    protected DeviceAlertSubscription(){

    }
    public DeviceAlertSubscription(Device device,Zone zone,Severity minSeverity){
        this.device=device;
        this.zone = zone;
        this.minSeverity = Objects.requireNonNull(minSeverity,"min Severity is required");
    }

    public void changeMinSeverity(Severity minSeverity){
        this.minSeverity = Objects.requireNonNull(minSeverity,"minSeverity is required");
    }

    public void deactivate(){
        this.active = false;
    }
    public Device getDevice(){
        return device;
    }

    public Zone getZone(){
        return zone;
    }

    public Severity getMinSeverity(){
        return minSeverity;
    }

    public boolean isActive() {
        return active;
    }
}
