package com.coelhotechne.detection_system.detection.domain;


import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorNiche;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DetectionTypeRulesTest {
    @Test
    void everyNicheClassified(){
        Set<SensorNiche> classified = EnumSet.noneOf(SensorNiche.class);
        classified.addAll(DetectionTypeRules.nichesWithDetectionRules());
        classified.addAll(DetectionTypeRules.MEASUREMENT_ONLY_NICHES);
        assertThat(classified).containsExactlyInAnyOrder(SensorNiche.values());
    }
    @Test
    void noNicheIsInBothLists(){
        assertThat(DetectionTypeRules.nichesWithDetectionRules())
                .doesNotContainAnyElementsOf(DetectionTypeRules.MEASUREMENT_ONLY_NICHES);
    }
    @Test
    void nicheCanReportItsOwnSubject(){
        assertThat(DetectionTypeRules.allowsSensor(SensorNiche.PRESENCE, DetectionType.PRESENCE)).isTrue();
        assertThat(DetectionTypeRules.allowsSensor(SensorNiche.ANIMAL, DetectionType.ANIMAL)).isTrue();
        assertThat(DetectionTypeRules.allowsSensor(SensorNiche.VEHICLE, DetectionType.VEHICLE)).isTrue();
    }
    @ParameterizedTest
    @EnumSource(SensorNiche.class)
    void  universalTypesAllowedForEveryNiche(SensorNiche niche){
        assertThat(DetectionTypeRules.allowsSensor(niche,DetectionType.UNKNOWN)).isTrue();
        assertThat(DetectionTypeRules.allowsSensor(niche,DetectionType.TAMPERING)).isTrue();
    }
    @Test
    void cameraRejectsSensorOnlyTypes(){
        assertThat(DetectionTypeRules.allowsCamera(DetectionType.GAS_LEAK)).isFalse();
        assertThat(DetectionTypeRules.allowsCamera(DetectionType.PERSON)).isTrue();
    }
    @Test
    void nullTypeIsNeverAllowed(){
        assertThat(DetectionTypeRules.allowsSensor(SensorNiche.SECURITY,null)).isFalse();
        assertThat(DetectionTypeRules.allowsCamera(null)).isFalse();
    }
}
