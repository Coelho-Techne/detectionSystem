package com.coelhotechne.detection_system.device.domain;

import com.coelhotechne.detection_system.globalClass.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "device")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EntityListeners(AuditingEntityListener.class)
public class Device extends BaseEntity {
    @Column(name = "device_name")
    private String deviceName;
    @Column(name = "device_type")
    private String deviceType;
    @Column(name = "ip_adress")
    private String ipAdress;
    @Column(nullable = false)
    private  Boolean status;
    @Column(name = "last_access",nullable = false)
    private LocalDateTime lastCommunication;
    @Column(unique = true,name = "access_key",nullable = false)
    private String accessKey;

    public void issueAccessKey(String accessKeyHash) {
        this.accessKey = java.util.Objects.requireNonNull(accessKeyHash, "accessKeyHash is required");
    }
}
