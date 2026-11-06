package com.coelhotechne.detection_system.integration;

import com.coelhotechne.detection_system.alert.domain.Alert;
import com.coelhotechne.detection_system.alert.infrastructure.AlertRepository;
import com.coelhotechne.detection_system.batterysupply.domain.PowerSupply;
import com.coelhotechne.detection_system.batterysupply.domain.PowerSupplyType;
import com.coelhotechne.detection_system.detection.domain.SensorDetection;
import com.coelhotechne.detection_system.detection.infrastructure.DetectionRepository;
import com.coelhotechne.detection_system.sensor.application.event.SensorEventHandler;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorNiche;
import com.coelhotechne.detection_system.sensor.domain.homologation.SensorSelfTestOutcome;
import com.coelhotechne.detection_system.sensor.infrastructure.SensorRepository;
import com.coelhotechne.detection_system.telemetry.infrastructure.TelemetryRepository;
import com.coelhotechne.detection_system.zone.domain.Zone;
import com.coelhotechne.detection_system.zone.domain.enums.ZoneType;
import com.coelhotechne.detection_system.zone.infrastructure.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "IT_DB_URL",matches = ".+")
class MariaDbPipelineIT {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url",()-> System.getenv("IT_DB_URL"));
        registry.add("spring.datasource.username",() -> envOr("IT_DB_USER","root"));
        registry.add("spring.datasource.password",()-> envOr("IT_DB_PASSWORD",""));
        registry.add("spring.jpa.hibernate.ddl-auto",()->"validate");
        registry.add("spring.flyway.enabled",()->"true");

        registry.add("spring.jpa.defer-datasource-initialization",()->"false");
        registry.add("spring.sql.init.mode",()->"never");
    }
    @Autowired
    SensorEventHandler handler;
    @Autowired
    SensorRepository sensorRepository;
    @Autowired
    ZoneRepository zoneRepository;
    @Autowired
    DetectionRepository detectionRepository;
    @Autowired
    AlertRepository alertRepository;
    @Autowired
    TelemetryRepository telemetryRepository;
    @Autowired
    TransactionTemplate template;

    private String zoneName;
    private String sensorName;
    private UUID sensorId;


    @BeforeEach
    void setup(){
        String suffix = Long.toString(System.nanoTime(),36);
        UUID eventId = UUID.randomUUID();
        zoneName = "it-"+suffix;
        sensorName = "gas"+suffix.substring(Math.max(0,suffix.length()-8));
        template.executeWithoutResult(s -> {
            Zone zone = new Zone();
            zone.setName(zoneName);
            zone.setZoneType(ZoneType.INTERNAL);
            zone.setCenterLatitude(new BigDecimal("-23.5"));
            zone.setCenterLongitude(new BigDecimal("-46.6"));
            zone.setRadiusMeters(10.0);
            zone.setActive(true);
            zoneRepository.save(zone);

            Sensor sensor = new Sensor();
            sensor.setName(sensorName);
            sensor.setSensorNiche(SensorNiche.GAS);
            sensor.setDataDescription("integration test");
            sensor.setActivationTime(LocalDateTime.now());
            sensor.setZone(zone);
            sensor.setPowerSupply(new PowerSupply(PowerSupplyType.BATTERY,true));
            sensorId = sensorRepository.save(sensor).getUuid();
        });
    }

    @Test
    void detectionIsRecordedAndOpensOneAlertThatRepeats(){
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        UUID eventId=UUID.randomUUID();
        String topic = "home/%s/%s/detection".formatted(zoneName,sensorName);

        handler.handle(topic,detectionJson(eventId,now,"methane"));
        handler.handle(topic,detectionJson(eventId,now,"methane"));
        handler.handle(topic,detectionJson(UUID.randomUUID(),
                now.plusSeconds(1),"methane"));

        List<SensorDetection>detections = template.execute(s -> detectionRepository
                .findAll()
                .stream()
                .filter(d -> d instanceof  SensorDetection sensorDetection && sensorDetection.getSensor().getUuid().equals(sensorId))
                .map(d ->(SensorDetection) d)
                .toList());
        assertThat(detections).hasSize(2);
        assertThat(detections.get(0).getMeasuredValue()).isEqualByComparingTo("850");
        assertThat(detections.get(0).getConfidence()).isEqualByComparingTo("0.9");
        assertThat(detections.get(0).getGasType()).hasToString("METHANE");

        List<Alert>alerts=template.execute(s -> alertRepository
                .findAll()
                .stream()
                .filter(a -> sensorId.equals(a.getSourceId()))
                .toList());
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getOccurrenceCount()).isEqualTo(2);
        //Sensor ainda nao homologado: risco e vida rebaixa so HIGH
        assertThat(alerts.get(0).getSeverity()).hasToString("HIGH");
    }
    @Test
    void homologatedSensorRaisesCriticalGasAlert(){
        template.executeWithoutResult(
                s ->{
                    Sensor sensor = sensorRepository.findById(sensorId).orElseThrow();
                    UUID requestId = UUID.randomUUID();
                    Instant now = Instant.now();
                    sensor.getHomologation().startTest(requestId,now);
                    sensor.getHomologation().applyTestResult(requestId,
                            SensorSelfTestOutcome.passed("ok"),now);
                });
        String topic = "home/%s/%s/detection".formatted(zoneName,sensorName);
        handler.handle(topic,detectionJson(UUID.randomUUID(),Instant.now().truncatedTo(ChronoUnit.SECONDS),"hydrogen_sulfide"));
        List<Alert>alerts = template.execute(s -> alertRepository
                .findAll()
                .stream()
                .filter(a -> sensorId.equals(a.getSourceId())).toList());
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).getSeverity()).hasToString("CRITICAL");
        assertThat(alerts.get(0).getDedupKey()).contains("TOXIC_GAS");
    }
    @Test
    void selfTestResponseClosesPendingTest(){
        UUID requestId = UUID.randomUUID();
        template.executeWithoutResult(s -> {
            Sensor sensor = sensorRepository.findById(sensorId).orElseThrow();
            sensor.setAccessKey("hash-"+requestId);
            sensor.getHomologation().startTest(requestId,Instant.now());
        });
        handler.handle("home/%s/%s/selftest".formatted(zoneName,sensorName), """
                {"request_id":"%s","passed":true,"firmware_version":"1.4.2","checks":{"sensor_read":true}}
                """.formatted(requestId));
        String status = template.execute( s->
            sensorRepository
                    .findById(sensorId)
                    .orElseThrow()
                    .getHomologation()
                    .getStatus()
                    .name());
            assertThat(status).isEqualTo("PENDING_APPROVAL");
    }
    @Test
    void telemetryReadingIsStoredAndAggregated(){
        Instant measuredAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        String topic = "home/%s/%s/telemetry".formatted(zoneName,sensorName);
        String json = """
                {"v":2,"msg_id":"%s","measured_at":"%s",
                 "readings":[{"metric":"gas_concentration","value":120.5,"unit":"ppm"}],
                 "diagnostics":{"status":true,"power_percentage":80,"power_charging":false}}
                """.formatted(UUID.randomUUID(), measuredAt);
        handler.handle(topic,json);
        var series = telemetryRepository.findSeries(sensorId,"GAS_CONCENTRATION",
                measuredAt.minusSeconds(60),
                measuredAt.plusSeconds(60),60);
        assertThat(series).isNotEmpty();
        assertThat(series.get(0).getAvgValue().doubleValue()).isEqualTo(120.5);
        assertThat(series.get(0).getMaxValue().doubleValue()).isEqualTo(120.5);
    }

    private static String detectionJson(UUID eventId, Instant at, String gas){
        return """
                 {"v":2,"event_id":"%s","category":"gas_leak","gas":"%s",
                 "detected_at":"%s","confidence":0.9,"value":850,"unit":"ppm"}
                """.formatted(eventId,gas,at);
    }

    private static String envOr(String name, String fallback){
        String value = System.getenv(name);
        return value == null ? fallback:value;
    }
}
