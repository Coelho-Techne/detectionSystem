package com.coelhotechne.detection_system.sentinelRegistry.domain.enums;

public enum DroneType {
    // Multirrotores
    TRICOPTER,
    QUADCOPTER,
    HEXACOPTER,
    OCTOCOPTER,
    MULTIROTOR,

    // Asa fixa
    FIXED_WING,
    VTOL,
    HYBRID_WING,

    // Helicópteros
    SINGLE_ROTOR,
    COAXIAL_ROTOR,

    // Aeróstatos
    BALLOON,
    AIRSHIP,

    // Drones subaquáticos
    UNDERWATER_ROV,
    UNDERWATER_AUV,

    // Configurações especiais
    FPV,
    NANO_DRONE,
    MICRO_DRONE,
    KAMIKAZE,
    ARMED,
    SHOCK,
    GAS,


    // Outros
    OTHER
}
