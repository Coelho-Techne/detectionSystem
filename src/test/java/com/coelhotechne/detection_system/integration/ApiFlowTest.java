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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(properties = {
        "app.bootstrap.admin-email=admin@it.local",
        "app.bootstrap.admin-password=admin-password-it"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiFlowTest {

    @Autowired
    MockMvc mvc;

    private String adminToken;

    @BeforeEach
    void login()throws Exception{
        adminToken = token("admin@it.local","admin-password-it");
    }
    @Test
    void zoneLocationSensorAndAccessKey()throws Exception{
        String zoneId = createZone("casa-"+suffix());

        mvc.perform(auth(put("/api/v1/zone/{id}",zoneId),adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(zoneJson("desactived-house-"+suffix(),false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.created_by").value("admin@it.local"))
                .andExpect(jsonPath("$.created_at").isNotEmpty());

        String locationId = JsonPath.read(mvc.perform(auth(post("/api/v1/location"),adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"sala\",\"latitude\":-23.5,\"longitude\":-46.6,\"zone_uuid\":\"%s\"}".formatted(zoneId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(),"$.uuid");

        String sensorJson = """
                {"name":"pir%s","sensor_niche":"SECURITY","data_description":"porta","zone_uuid":"%s",
                 "installation":{"location_id":"%s","installed_by":"tec","installation_status":"ACTIVE"},
                 "power_supply":{"type":"BATTERY","rechargeable":true}}
                """.formatted(suffix().substring(0, 6), zoneId, locationId);
        String sensorBody = mvc.perform(auth(post("/api/v1/sensor"),adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(sensorJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.installation.location_id").value(locationId))
                .andReturn().getResponse().getContentAsString();
        String sensorId = JsonPath.read(sensorBody,"$.uuid");
        Integer version = JsonPath.read(sensorBody,"$.version");

        mvc.perform(auth(patch("/api/v1/sensor/{id}",sensorId),adminToken)
                .header(HttpHeaders.IF_MATCH,"\""+version+"\"")
                .contentType(MediaType.APPLICATION_JSON).content("{\"data_description\":\"porta da frente\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data_description").value("porta da frente"));

        mvc.perform(auth(post("/api/v1/sensor/{id}/commands/SELF-TEST",sensorId),adminToken))
                .andExpect(status().isBadRequest());

        mvc.perform(auth(post("/api/v1/sensor/{id}/access-key",sensorId),adminToken))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL,"no-store"))
                .andExpect(jsonPath("$.access_key",hasLength(43)));
        mvc.perform(auth(delete("/api/v1/zone/{id}",zoneId),adminToken)).andExpect(status().isConflict());
    }
    @Test
    void cameraKeyAuthAndDetectionIngest()throws Exception{
        String zoneId = createZone("garagem-"+suffix());
        String camJson = """
                {"cam_model":"X1","firmware_version":"1.0","serial_number":"SN%s","mac_address":"MAC%s",
                 "ip_address":"10.0.0.9","zone_id":"%s","detection_confidence_threshold":0.5}
                """.formatted(suffix(), suffix(), zoneId);
        String camId = JsonPath.read(mvc.perform(auth(post("/api/v1/cam-fixed"),adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(camJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.has_access_key").value(false))
                .andReturn().getResponse().getContentAsString(),"$.uuid");
        String key = JsonPath.read(mvc.perform(auth(post("/api/v1/cam/{id}/access-key",camId),adminToken))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(),"$.access_key");

        mvc.perform(post("/api/v1/cam/{id}/auth",camId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"access_key\":\"%s\"}".formatted(key)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("AUTORIZADO"));
        mvc.perform(post("/api/v1/cam/{id}/auth",camId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"access_key\":\"errada\"}"))
                .andExpect(status().isUnauthorized());

        String detection = """
                {"event_id":"%s","type":"person","confidence":0.91,"detected_at":"%s",
                 "track_id":7,"bbox":[10,20,100,200]}
                """.formatted(UUID.randomUUID(),Instant.now());
        mvc.perform(post("/api/v1/cam/{id}/detections",camId)
                .header("X-Device-Id",camId).header("X-Access-Key",key)
                .contentType(MediaType.APPLICATION_JSON).content(detection))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("PERSON"));

        mvc.perform(post("/api/v1/cam/{id}/detections",camId)
                .header("X-Device-Id",camId).header("X-Access-Key",key)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"event_id\":\"%s\",\"type\":\"person\",\"confidence\":0.2}".formatted(UUID.randomUUID())))
                .andExpect(status().isAccepted());

        mvc.perform(auth(get("/api/v1/cam/{id}/detections",camId).param("type","PERSON"),adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content",hasSize(1)));
        mvc.perform(auth(get("/api/v1/cam/{id}/detections",camId).param("type","VEHICLE"),adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content",hasSize(0)));

        mvc.perform(auth(post("/api/v1/cam/{id}/homologation/test",camId),adminToken))
                .andExpect(status().isConflict());
        mvc.perform(auth(post("/api/v1/cam/{id}/connection",camId),adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"protocol\":\"RTSP\",\"stream_uri\":\"rtsp://10.0.0.9:554/s1\",\"link_type\":\"ETHERNET\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_TEST"));

    }
    @Test
    void deviceLifecycleAndSubscription()throws Exception{
        String body = mvc.perform(auth(post("/api/v1/device"),adminToken).contentType(MediaType.APPLICATION_JSON)
                .content("{\"device_name\":\"painel\",\"device_type\":\"PANEL\",\"ip_address\":\"10.0.0.2\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION,startsWith("/api/v1/device")))
                .andReturn().getResponse().getContentAsString();
        String deviceId = JsonPath.read(body,"$.uuid");
        String key= JsonPath.read(body,"$.access_key");

        mvc.perform(post("/api/v1/device/{id}/authenticate",deviceId)
                .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"access_key\":\"%s\"}".formatted(key)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("AUTHORIZED"));

        mvc.perform(auth(put("/api/v1/device/{id}/subscriptions",deviceId),adminToken)
                .contentType(MediaType.APPLICATION_JSON).content("{\"min_severity\":\"HIGH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.min_severity").value("HIGH"));
        mvc.perform(auth(get("/api/v1/device/{id}/subscriptions",deviceId),adminToken))
                .andExpect(jsonPath("$",hasSize(1)));

        mvc.perform(auth(get("/api/v1/device/{id}",UUID.randomUUID()),adminToken)).andExpect(status().isNotFound());

        mvc.perform(auth(delete("/api/v1/device/{id}",deviceId),adminToken))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }
    @Test
    void userManagement()throws Exception{
        String email = "tec"+suffix()+"@it.local";
        mvc.perform(auth(post("/api/v1/user"),adminToken).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"senha-forte-123\",\"role\":\"TECHNICIAN\"}".formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password_hash").doesNotExist());

        String techToken = token(email,"senha-forte-123");
        mvc.perform(auth(get("/api/v1/auth/me"),techToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("TECHNICIAN"));
        mvc.perform(auth(get("/api/v1/user"),techToken)).andExpect(status().isForbidden());

        mvc.perform(auth(post("/api/v1/auth/password"),techToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"current_password\":\"senha-forte-123\",\"new_password\":\"outra-senha-456\"}"))
                .andExpect(status().isNoContent());
        assertThat(token(email,"outra-senha-456")).isNotBlank();
    }
    // :::::::::::::::::::::::::::: util ::::::::::::::::::::::::::::
    private String token(String email, String password)throws Exception{
        return JsonPath.read(mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email,password))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),"$.token");
    }

    private String createZone(String name)throws Exception{
        ResultActions result = mvc.perform(auth(post("/api/v1/zone"),adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(zoneJson(name,true)))
                .andExpect(status().isCreated());
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(),"$.uuid");
    }

    private static String zoneJson(String name, boolean active){
        return"""
                {"name":"%s","zone_type":"INTERNAL","center_latitude":-23.5,"center_longitude":-46.6,
                 "radius_meters":20,"active":%s}
                """.formatted(name, active);
    }

    private static MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder requestBuilder,String token){
        return requestBuilder.header(HttpHeaders.AUTHORIZATION,"Bearer "+token);
    }
    private static String suffix(){
        return Long.toString(System.nanoTime(),36);
    }
}
