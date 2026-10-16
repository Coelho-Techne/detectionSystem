package com.coelhotechne.detection_system.telemetry.domain.enums;

import com.coelhotechne.detection_system.sensor.domain.enums.SensorNiche;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public enum TelemetryMetric {
    // ---- Diagnóstico do dispositivo (amostrado) ----
    MEMORY_USED("%", true),
    DATA_TRANSFER("%", true),
    BATTERY_PERCENT("%", true),
    RSSI("dBm", true),

    // ---- Ambiente / clima ----
    TEMPERATURE("°C", false, SensorNiche.ENVIRONMENT, SensorNiche.WEATHER, SensorNiche.FIRE, SensorNiche.HEALTH),
    HUMIDITY("%", false, SensorNiche.ENVIRONMENT, SensorNiche.WEATHER),
    PRESSURE("hPa", false, SensorNiche.ENVIRONMENT, SensorNiche.WEATHER),
    RAINFALL("mm", false, SensorNiche.WEATHER),
    WIND_SPEED("m/s", false, SensorNiche.WEATHER),
    UV_INDEX("", false, SensorNiche.WEATHER),

    // ---- Qualidade do ar / gás ----
    CO2("ppm", false, SensorNiche.AIR_QUALITY),
    VOC("ppb", false, SensorNiche.AIR_QUALITY),
    PM2_5("µg/m³", false, SensorNiche.AIR_QUALITY),
    PM10("µg/m³", false, SensorNiche.AIR_QUALITY),
    GAS_CONCENTRATION("ppm", false, SensorNiche.GAS, SensorNiche.AIR_QUALITY),

    // ---- Energia / iluminação / som ----
    POWER("W", false, SensorNiche.ENERGY),
    ENERGY("kWh", false, SensorNiche.ENERGY),
    VOLTAGE("V", false, SensorNiche.ENERGY),
    CURRENT("A", false, SensorNiche.ENERGY),
    ILLUMINANCE("lx", false, SensorNiche.LIGHTING),
    SOUND_LEVEL("dB", false, SensorNiche.SOUND),

    // ---- Água / piscina / solo ----
    WATER_LEVEL("cm", false, SensorNiche.HYDRAULIC, SensorNiche.POOL),
    FLOW_RATE("L/min", false, SensorNiche.HYDRAULIC),
    PH("pH", false, SensorNiche.POOL, SensorNiche.SOIL),
    CHLORINE("ppm", false, SensorNiche.POOL),
    TURBIDITY("NTU", false, SensorNiche.POOL),
    WATER_TEMPERATURE("°C", false, SensorNiche.POOL),
    SOIL_MOISTURE("%", false, SensorNiche.SOIL),
    SOIL_TEMPERATURE("°C", false, SensorNiche.SOIL),

    // ---- Posição / peso / saúde ----
    DISTANCE("m", false, SensorNiche.POSITION, SensorNiche.VEHICLE),
    TILT("°", false, SensorNiche.POSITION, SensorNiche.STRUCTURAL),
    WEIGHT("kg", false, SensorNiche.WEIGHT, SensorNiche.ANIMAL),
    HEART_RATE("bpm", false, SensorNiche.HEALTH),

    /** Só para as linhas manuais antigas (migração); nunca aceito na entrada. */
    UNKNOWN("", false);
    private  final String unit;
    private  final boolean diagnostic;
    private  final Set<SensorNiche>niches;

    TelemetryMetric(String unit, boolean diagnostic,SensorNiche... niches){
        this.unit=unit;
        this.diagnostic=diagnostic;
        this.niches = niches.length == 0 ? Collections.emptySet():
                Collections.unmodifiableSet(EnumSet.of(niches[0],niches));
    }

    public String getUnit(){
        return unit;
    }
    public boolean isDiagnostic(){
        return diagnostic;
    }

    public boolean allowsNiche(SensorNiche niche){
        if (this == UNKNOWN){
            return false;
        }
        return diagnostic || (niche!=null && niches.contains(niche));
    }

    public static TelemetryMetric fromCode(String code){
        if (code == null || code.isBlank()){
            return null;
        }
        String normalized = code.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-','_')
                .replace(' ','_')
                .replace('.','_');
        for (TelemetryMetric metric:values()){
            if (metric != UNKNOWN && metric.name().equals(normalized)){
                return metric;
            }
        }
        return null;
    }
}
