package com.coelhotechne.detection_system.detection.domain.enums;

import java.util.Locale;

public enum DetectionType {
    MOTION("Motion"),
    PRESENCE("Presence"),
    DOOR_OPEN("Door Open"),
    WINDOW_OPEN("Window Open"),
    GLASS_BREAK("Glass Break"),
    VIBRATION("Vibration"),
    IMPACT("Impact"),
    TAMPERING("Tampering"),

    // Visão (câmera / IA)
    PERSON("Person"),
    VEHICLE("Vehicle"),
    ANIMAL("Animal"),
    OBJECT("Object"),

    // Fogo / gás
    SMOKE("Smoke"),
    FIRE("Fire"),
    HIGH_HEAT("High Heat"),
    GAS_LEAK("Gas Leak"),
    CARBON_MONOXIDE("Carbon Monoxide"),
    AIR_HUMIDITY("Air Humidity"),
    TOXIC_GAS("Toxic Gas"),
    /** Oxigênio abaixo do seguro — gás inerte (N2, Ar, He) deslocando o ar. */
    OXYGEN_DEFICIENCY("Oxygen Deficiency"),

    // Água
    WATER_LEAK("Water Leak"),
    FLOOD("Flood"),

    // Pessoas
    FALL("Fall"),

    UNKNOWN("Unknown");

    private final String label;

    DetectionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Converte o código enviado pela origem. Tolera caixa, espaços e hífen
     * Nunca lança: código desconhecido vira {@link #UNKNOWN} — quem chama decide se loga.
     */
    public static DetectionType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return UNKNOWN;
        }
        String normalized = code.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        for (DetectionType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
