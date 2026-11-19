package com.coelhotechne.detection_system.serverCoupling.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "system_node")
public class SystemNode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID systemId;

    @Column(nullable = false,unique = true,length = 100)
    private String nodeIdentifier;

    private String hostname;
    private String machineId;
    private String bootId;
    private String operatingSystem;
    private String kernel;
    private String architecture;
    private String hardwareVendor;
    private String hardwareModel;
    private String firmwareVersion;
    private String firmwareDate;
    private String firmwareAge;

    private Instant registeredAt;
    private Instant lastSeeAt;
}
