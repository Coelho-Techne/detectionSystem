package com.coelhotechne.detection_system.device.domain;

import com.coelhotechne.detection_system.globalClass.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "device")
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
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
