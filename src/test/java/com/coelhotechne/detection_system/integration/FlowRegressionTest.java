package com.coelhotechne.detection_system.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "app.bootstrap.admin-email=admin@regression.local",
        "app.bootstrap.admin-password=admin-password-regression"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FlowRegressionTest {

    @Autowired
    MockMvc mvc;

    private String adminToken;

    @BeforeEach
    void login() throws Exception{
        adminToken = JsonPath.read(mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@regression.local\",\"password\":\"admin-password-regression\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(),"$.token");
    }
    @Test
    void telemetrySearchFindsReadingsInsideTheWindow()throws Exception{
        String zoneId= createZone();
        String sensorJson = """
                {"name":"temp%s","sensor_niche":"ENVIRONMENT","data_description":"sala","zone_uuid":"%s",
                 "installation":{"installed_by":"tec","installation_status":"ACTIVE"}}
                """.formatted(suffix().substring(0,6),zoneId);
        String sensorId = JsonPath.read(mvc.perform(auth(post("/api/v1/sensor"),adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(sensorJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(),"$.uuid");

        Instant measuredAt = Instant.now().minusSeconds(120);
        mvc.perform(auth(post("/api/v1/telemetry"),adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sensor_id\":\"%s\",\"metric\":\"temperature\",\"value\":21.5,\"measured_at\":\"%s\"}"
                        .formatted(sensorId,measuredAt)))
                .andExpect(status().isCreated());

        mvc.perform(auth(get("/api/v1/telemetry").param("sensor_id",sensorId),adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content",hasSize(1)))
                .andExpect(jsonPath("$.content[0].metric").value("TEMPERATURE"))
                .andExpect(jsonPath("$.content[0].measured_at",startsWith(
                        measuredAt.toString().substring(0,19)
                ))).andExpect(jsonPath("$.content[0].received_at",
                        not(startsWith(measuredAt.toString().substring(0,19)))));
    }
    @Test
    void camFixedUpdateIsApplied()throws Exception{
        String zoneId = createZone();
        String camId = JsonPath.read(mvc.perform(auth(post("/api/v1/cam-fixed"),adminToken).contentType(MediaType.APPLICATION_JSON)
                .content(camJson("X1",zoneId,"0.5")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.detection_confidence_threshold").value(0.5))
                .andReturn().getResponse().getContentAsString(),"$.uuid");

        mvc.perform(auth(put("/api/v1/cam-fixed/{id}",camId),adminToken).contentType(MediaType.APPLICATION_JSON)
                .content(camJson("X2",zoneId,"0.8")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cam_model")
                        .value("X2"))
                .andExpect(jsonPath("$.detection_confidence_threshold").value(closeTo(0.8,0.0001)));

        mvc.perform(auth(get("/api/v1/cam-fixed/{id}",camId),adminToken))
                .andExpect(jsonPath("$.cam_model").value("X2"));
    }
    @Test
    void detectionLocationIsReadable()throws Exception{
        String zoneId = createZone();
        String camId = JsonPath.read(mvc.perform(auth(post("/api/v1/cam-fixed"),adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(camJson("X1",zoneId,"0.5")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(),"$.uuid");
        String key =JsonPath.read(mvc.perform(auth(post("/api/v1/cam/{id}/access-key",camId),adminToken))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(),"$.access_key");
        String eventId = UUID.randomUUID().toString();
        String detection =  """
                {"event_id":"%s","type":"person","confidence":0.91,"detected_at":"%s","track_id":3}
                """.formatted(eventId,Instant.now());
        String location = mvc.perform(post("/api/v1/cam/{id}/detections",camId)
                .header("X-Device-Id",camId).header("X-Access-Key",key)
                .contentType(MediaType.APPLICATION_JSON).content(detection)).andExpect(status().isCreated())
                .andReturn().getResponse().getHeader(HttpHeaders.LOCATION);

        mvc.perform(auth(get(location),adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.camera_block.uuid").value(camId))
                .andExpect(jsonPath("$.camera_block.track_id").value(3));
        mvc.perform(auth(get("/api/v1/detection").param("source_id",camId),adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content",hasSize(1)));

        mvc.perform(post("/api/v1/cam/{id}/detections",camId)
                .header("X-Device-Id",camId).header("X-Access-Key",key)
                .contentType(MediaType.APPLICATION_JSON).content(detection))
                .andExpect(status().isOk());
        mvc.perform(auth(get("/api/v1/detection").param("source_id",camId),adminToken))
                .andExpect(jsonPath("$.content",hasSize(1)));
    }

    private String createZone()throws Exception{
        return JsonPath.read(mvc.perform(auth(post("/api/v1/zone"),adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                {"name":"z-%s","zone_type":"INTERNAL","center_latitude":-23.5,"center_longitude":-46.6,
                                 "radius_meters":20,"active":true}
                                """.formatted(suffix())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(),"$.uuid");
    }

    private static String camJson(String model, String zoneId,String threshold){
        String s = suffix();
        return """
                {"cam_model":"%s","firmware_version":"1.0","serial_number":"SN%s","mac_address":"MAC%s",
                 "ip_address":"10.0.0.9","zone_id":"%s","detection_confidence_threshold":%s}
                """.formatted(model,s,s,zoneId,threshold);
    }

    private static MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder b,String token){
        return b.header(HttpHeaders.AUTHORIZATION,"Bearer "+token);
    }
    private static String suffix(){
        return Long.toString(System.nanoTime(),36);
    }
}
