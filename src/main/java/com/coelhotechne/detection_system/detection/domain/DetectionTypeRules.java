package com.coelhotechne.detection_system.detection.domain;

import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorNiche;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class DetectionTypeRules {
    private static final Set<DetectionType>UNIVERSAL = Collections
            .unmodifiableSet(EnumSet.of(DetectionType.UNKNOWN,DetectionType.TAMPERING));
    private static final Map<SensorNiche, Set<DetectionType>> SENSOR_RULES = buildSensorRules();
    private static final Set<DetectionType> CAMERA_RULES = EnumSet.of(
            DetectionType.ANIMAL,
            DetectionType.VEHICLE,
            DetectionType.OBJECT,
            DetectionType.PERSON,
            DetectionType.MOTION);

    public DetectionTypeRules(){}
    private static Map<SensorNiche,Set<DetectionType>> buildSensorRules() {
        Map<SensorNiche,Set<DetectionType>> rules = new EnumMap<>(SensorNiche.class);
        rules.put(SensorNiche.SECURITY, EnumSet.of(DetectionType.DOOR_OPEN,
                DetectionType.WINDOW_OPEN,
                DetectionType.GLASS_BREAK,
                DetectionType.TAMPERING,
                DetectionType.IMPACT,
                DetectionType.MOTION));
        rules.put(SensorNiche.PRESENCE,EnumSet.of(DetectionType.ANIMAL,DetectionType.PERSON));
        rules.put(SensorNiche.HYDRAULIC,EnumSet.of(DetectionType.FLOOD,DetectionType.WATER_LEAK));
        rules.put(SensorNiche.GAS,EnumSet.of(DetectionType.GAS_LEAK,DetectionType.CARBON_MONOXIDE));
        rules.put(SensorNiche.FIRE,EnumSet.of(DetectionType.FIRE,DetectionType.SMOKE,DetectionType.HIGH_HEAT));
        rules.put(SensorNiche.STRUCTURAL,EnumSet.of(DetectionType.VIBRATION,DetectionType.IMPACT));
        rules.put(SensorNiche.HEALTH,EnumSet.of(DetectionType.FALL));
        rules.replaceAll((sensorNiche,
                          detectionTypes) ->
                Collections.unmodifiableSet(detectionTypes));
        return Collections.unmodifiableMap(rules);
    }

    public static boolean allowsSensor(SensorNiche niche,DetectionType type){
        if (type==null){
            return false;
        }
        if (UNIVERSAL.contains(type)){
            return true;
        }
        return niche!=null && SENSOR_RULES.getOrDefault(niche,Set.of()).contains(type);
    }
    public static boolean allowsCamera(DetectionType type){
        return type != null && (UNIVERSAL.contains(type)||CAMERA_RULES.contains(type));
    }
}
