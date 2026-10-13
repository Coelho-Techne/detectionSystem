package com.coelhotechne.detection_system.detection.domain.enums;

import java.util.Locale;

public enum DetectionGasType {
    // ───────────── Combustíveis (catalítico, NDIR, PID) → GAS_LEAK ─────────────
    METHANE(GasHazard.COMBUSTIBLE, "Methane (Natural Gas)"),
    PROPANE(GasHazard.COMBUSTIBLE, "Propane"),
    BUTANE(GasHazard.COMBUSTIBLE, "Butane"),
    /** GLP de botijão: mistura de propano e butano. Sensor residencial costuma reportar assim. */
    LPG(GasHazard.COMBUSTIBLE, "LPG"),
    HYDROGEN(GasHazard.COMBUSTIBLE, "Hydrogen"),
    ACETYLENE(GasHazard.COMBUSTIBLE, "Acetylene"),
    /** Etanol combustível na garagem é o caso real; álcool 70% e perfume geram falso alarme. */
    ETHANOL_VAPOR(GasHazard.COMBUSTIBLE, "Ethanol Vapor"),
    ISOPROPANOL_VAPOR(GasHazard.COMBUSTIBLE, "Isopropanol Vapor"),
    ACETONE_VAPOR(GasHazard.COMBUSTIBLE, "Acetone Vapor"),

    // ───────────── Asfixiantes simples → OXYGEN_DEFICIENCY ─────────────
    // Não são identificados quimicamente: o sensor mede O2 baixo. O gás vem da configuração do
    // ponto (sensor de O2 instalado junto ao cilindro/tanque daquele gás).
    NITROGEN(GasHazard.ASPHYXIANT, "Nitrogen"),
    ARGON(GasHazard.ASPHYXIANT, "Argon"),
    HELIUM(GasHazard.ASPHYXIANT, "Helium"),

    // ───────────── Tóxicos → TOXIC_GAS ─────────────
    // Asfixiantes químicos (eletroquímico)
    CARBON_MONOXIDE(GasHazard.TOXIC, "Carbon Monoxide"),
    HYDROGEN_CYANIDE(GasHazard.TOXIC, "Hydrogen Cyanide"),
    /** NDIR. Abaixo do limiar de alarme é qualidade do ar (telemetria), não detecção. */
    CARBON_DIOXIDE(GasHazard.TOXIC, "Carbon Dioxide"),

    // Compostos de enxofre (eletroquímico)
    HYDROGEN_SULFIDE(GasHazard.TOXIC, "Hydrogen Sulfide"),
    SULFUR_DIOXIDE(GasHazard.TOXIC, "Sulfur Dioxide"),

    // Compostos de nitrogênio (eletroquímico)
    AMMONIA(GasHazard.TOXIC, "Ammonia"),
    NITROGEN_DIOXIDE(GasHazard.TOXIC, "Nitrogen Dioxide"),
    NITRIC_OXIDE(GasHazard.TOXIC, "Nitric Oxide"),
    HYDRAZINE(GasHazard.TOXIC, "Hydrazine"),

    // Hidretos (célula de hidretos — sensibilidade cruzada entre os dois)
    PHOSPHINE(GasHazard.TOXIC, "Phosphine"),
    ARSINE(GasHazard.TOXIC, "Arsine"),

    // Halogênios e ácidos (eletroquímico)
    CHLORINE(GasHazard.TOXIC, "Chlorine"),
    CHLORINE_DIOXIDE(GasHazard.TOXIC, "Chlorine Dioxide"),
    HYDROGEN_CHLORIDE(GasHazard.TOXIC, "Hydrogen Chloride"),
    HYDROGEN_FLUORIDE(GasHazard.TOXIC, "Hydrogen Fluoride"),
    PHOSGENE(GasHazard.TOXIC, "Phosgene"),

    // Fumigantes (detector dedicado de fumigação)
    SULFURYL_FLUORIDE(GasHazard.TOXIC, "Sulfuryl Fluoride"),
    METHYL_BROMIDE(GasHazard.TOXIC, "Methyl Bromide"),

    // Oxidantes e aldeídos
    OZONE(GasHazard.TOXIC, "Ozone"),
    FORMALDEHYDE(GasHazard.TOXIC, "Formaldehyde"),
    ACETALDEHYDE(GasHazard.TOXIC, "Acetaldehyde"),
    ETHYLENE_OXIDE(GasHazard.TOXIC, "Ethylene Oxide"),

    // VOCs (PID). O PID não separa compostos: o valor é "VOC expresso como X", conforme a calibração.
    VOC_TOTAL(GasHazard.TOXIC, "Total VOC"),
    BENZENE(GasHazard.TOXIC, "Benzene"),
    TOLUENE(GasHazard.TOXIC, "Toluene"),
    XYLENE(GasHazard.TOXIC, "Xylene"),
    VINYL_CHLORIDE(GasHazard.TOXIC, "Vinyl Chloride"),
    BUTADIENE_1_3(GasHazard.TOXIC, "1,3-Butadiene"),
    CARBON_DISULFIDE(GasHazard.TOXIC, "Carbon Disulfide"),

    // Detectores específicos
    MERCURY_VAPOR(GasHazard.TOXIC, "Mercury Vapor"),
    /** Média de longo prazo (Bq/m³), não pico: o alarme é a média passar do limite. */
    RADON(GasHazard.TOXIC, "Radon"),

    /** Código fora do catálogo. Vira GAS_LEAK: na dúvida, alarme, não silêncio. */
    UNKNOWN(GasHazard.UNKNOWN, "Unknown Gas");

    private final GasHazard hazard;
    private final String label;

    DetectionGasType(GasHazard hazard, String label) {
        this.hazard = hazard;
        this.label = label;
    }

    public GasHazard getHazard() {
        return hazard;
    }

    public String getLabel() {
        return label;
    }

    /** Tipo de detecção que este gás gera. Nunca devolve {@link DetectionType#UNKNOWN}. */
    public DetectionType detectionType() {
        return switch (hazard) {
            case TOXIC -> DetectionType.TOXIC_GAS;
            case ASPHYXIANT -> DetectionType.OXYGEN_DEFICIENCY;
            case COMBUSTIBLE, UNKNOWN -> DetectionType.GAS_LEAK;
        };
    }

    /**
     * Converte o código recebido da origem. Tolera maiúsculas, minúsculas, espaços e hífens.
     * Retorna {@link #UNKNOWN} para códigos nulos, vazios ou não reconhecidos.
     */
    public static DetectionGasType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return UNKNOWN;
        }
        String normalized = code.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        for (DetectionGasType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
