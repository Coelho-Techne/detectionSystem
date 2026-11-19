package com.coelhotechne.detection_system.cluster.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cluster_lease")
public class ClusterLease {

    @Id
    @Column(name = "name",length = 100)
    private String name;
    @Column(name = "owner", length = 64, nullable = false)
    private String owner;
    @Column(name = "locked_until", nullable = false)
    private Instant lockedUntil;
    @Column(name = "locked_at", nullable = false)
    private Instant lockedAt;

    public ClusterLease(String name, String owner, Instant now, Instant lockedUntil) {
        this.name = name;
        this.owner = owner;
        this.lockedAt = now;
        this.lockedUntil = lockedUntil;
    }
}
