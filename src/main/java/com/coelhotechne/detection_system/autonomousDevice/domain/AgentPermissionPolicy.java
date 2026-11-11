package com.coelhotechne.detection_system.autonomousDevice.domain;

import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomousCapability;

import java.util.*;

import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomousCapability.*;
import com.coelhotechne.detection_system.security.domain.enums.Role;

import static com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomousCapability.*;


public final class AgentPermissionPolicy {

    private static final Set<AutonomousCapability>READ  = EnumSet.of(
            DEVICE_READ, SENSOR_READ, CAMERA_READ, ALERT_READ, TELEMETRY_READ, REPORT_GENERATION
    );

    private static final Map<Role, Set<AutonomousCapability>> MATRIX = new EnumMap<>(Role.class);

    static {
        Set<AutonomousCapability> home = EnumSet.copyOf(READ);
        home.addAll(EnumSet.of(ALERT_ACKNOWLEDGE,ALERT_RESOLVE));

        Set<AutonomousCapability> technician = EnumSet.copyOf(home);
        technician.addAll(EnumSet.of(DEVICE_UPDATE,DEVICE_RESTART,DEVICE_DIAGNOSTICS,
                SENSOR_CALIBRATION,CAMERA_CONFIGURATION,SYSTEM_DIAGNOSTICS));

        Set<AutonomousCapability> admin = EnumSet.allOf(AutonomousCapability.class);

        MATRIX.put(Role.ADMIN, Collections.unmodifiableSet(admin));
        MATRIX.put(Role.TECHNICIAN, Collections.unmodifiableSet(technician));
        MATRIX.put(Role.HOME_USER, Collections.unmodifiableSet(home));
        MATRIX.put(Role.AI_USER, Collections.unmodifiableSet(EnumSet.copyOf(READ)));
        MATRIX.put(Role.DEVELOPER, Collections.emptySet());
        MATRIX.put(Role.DEVICE_USER, Collections.emptySet());
    }

    private AgentPermissionPolicy(){}

    public static Set<AutonomousCapability>capabilitiesOf(Role role){
        return  role == null ? Collections.emptySet() : MATRIX.getOrDefault(role,Collections.emptySet());
    }
    public static boolean permits(Role role,AutonomousCapability capability){
        return capability != null && capabilitiesOf(role).contains(capability);
    }
}
