package com.coelhotechne.detection_system.sensor.domain.payload;


import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SensorTelemetryPayloadTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void v1WithTypoKeys(){
        SensorTelemetryPayload p = mapper.readValue("""
                {"status":true,"memoru_used":41.2,"data_transfer_value":12,
                 "power_percentege":78,"power_charging":false}
                """, SensorTelemetryPayload.class);

        assertThat(p.memoryUsed()).isEqualByComparingTo("41.2");
        assertThat(p.powerPercentage()).isEqualByComparingTo("78");
        assertThat(p.hasPowerReading()).isTrue();
        assertThat(p.reading()).isEmpty();
        assertThat(p.msgId()).isNull();
    }

    @Test
    void v2WithReadingsAndDiagnosticsBlock(){
        SensorTelemetryPayload p = mapper.readValue(
                """
                {"v":2,"msg_id":"9b2f6d1e-4c0a-4f53-9a51-0d7c2a3e8b11","measured_at":"2026-10-09T23:41:05Z",
                 "readings":[{"metric":"temperature","value":23.5},{"metric":"humidity","value":61.0}],
                 "diagnostics":{"status":true,"memory_used":41.2,"power_percentage":78,"rssi_dbm":-67}}
                """, SensorTelemetryPayload.class);
        assertThat(p.version()).isEqualTo(2);
        assertThat(p.version()).isEqualTo(Instant.parse("2026-10-09T23:41:05Z"));
        assertThat(p.reading()).hasSize(2);
        assertThat(p.reading().get(0).metric()).isEqualTo("temperature");
        assertThat(p.memoryUsed()).isEqualByComparingTo("41.2");
        assertThat(p.rssiDbm()).isEqualTo(-67);
    }
    @Test
    void v1AndV2ProduceSameDiagnostics(){
        SensorTelemetryPayload v1 = mapper.readValue("""
                {"status":true,"memoru_used":10,"power_percentege":50}
                """, SensorTelemetryPayload.class);
        SensorTelemetryPayload v2 = mapper.readValue("""
                {"v":2,"diagnostics":{"status":true,"memory_used":10,"power_percentage":50}}
                """, SensorTelemetryPayload.class);
        assertThat(v2.status()).isEqualTo(v1.status());
        assertThat(v2.memoryUsed()).isEqualByComparingTo(v1.memoryUsed());
        assertThat(v2.powerPercentage()).isEqualByComparingTo(v1.powerPercentage());
    }
}
