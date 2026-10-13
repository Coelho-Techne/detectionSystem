package com.coelhotechne.detection_system.detection.domain;

import com.coelhotechne.detection_system.detection.domain.enums.DetectionGasType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorNiche;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DetectionGasTypeTest {

    @Test
    void fromCodeToleratesCaseSpacesAndHyphen() {
        assertEquals(DetectionGasType.CARBON_MONOXIDE, DetectionGasType.fromCode("carbon monoxide"));
        assertEquals(DetectionGasType.LPG, DetectionGasType.fromCode(" lpg "));
        assertEquals(DetectionGasType.BUTADIENE_1_3, DetectionGasType.fromCode("butadiene-1-3"));
        assertEquals(DetectionGasType.UNKNOWN, DetectionGasType.fromCode(null));
        assertEquals(DetectionGasType.UNKNOWN, DetectionGasType.fromCode("CO"));
    }

    @Test
    void hazardDefinesDetectionType() {
        assertEquals(DetectionType.GAS_LEAK, DetectionGasType.LPG.detectionType());
        assertEquals(DetectionType.TOXIC_GAS, DetectionGasType.CARBON_MONOXIDE.detectionType());
        assertEquals(DetectionType.OXYGEN_DEFICIENCY, DetectionGasType.NITROGEN.detectionType());
        assertEquals(DetectionType.GAS_LEAK, DetectionGasType.UNKNOWN.detectionType());
    }

    /** Um sensor de gás nunca pode ter a detecção descartada por causa do gás que reportou. */
    @Test
    void everyGasIsAcceptedFromGasNiche() {
        for (DetectionGasType gas : DetectionGasType.values()) {
            assertTrue(gas.detectionType() != DetectionType.UNKNOWN, gas.name());
            assertTrue(DetectionTypeRules.allowsSensor(SensorNiche.GAS, gas.detectionType()), gas.name());
        }
    }
}
