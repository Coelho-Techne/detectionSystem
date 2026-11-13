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

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "app.bootstrap.admin-email=admin@agent.local",
        "app.bootstrap.admin-password=admin-password-agent"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgentFlowTest {
    private static final String DEVICE_JSON =
            "{\"device_name\":\"painel\",\"device_type\":\"PANEL\",\"ip_address\":\"10.0.0.2\"}";

    @Autowired
    MockMvc mvc;

    private String adminToken;

    @BeforeEach
    void login()throws Exception{
        adminToken = token("admin@agent.local","admin-password-agent");
    }
    @Test
    void agentActsWithinRequesterRoleAndOwnLimits()throws Exception{
        String body = mvc.perform(auth(post("/api/v1/agent"),adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"agent_name":"assistente-%s","agent_type":"DEVICE_MAINTENANCE",
                                 "autonomy_level":"AUTONOMOUS","require_approval":false,
                                 "capabilities":["DEVICE_READ","DEVICE_UPDATE","ALERT_READ"]}
                                """.formatted(suffix())))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION,containsString("/api/v1/agent/")))
                .andExpect(jsonPath("$.agent_status").value("CREATED"))
                .andExpect(jsonPath("$.access_key").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String agentId = JsonPath.read(body,"$.uuid");
        String agentKey =JsonPath.read(body,"$.access_key");

        mvc.perform(auth(get("/api/v1/agent/{id}",agentId),adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_key").doesNotExist());

        String deviceId = JsonPath.read(mvc.perform(auth(post("/api/v1/device"),adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(DEVICE_JSON))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(),"$.uuid");
        mvc.perform(agent(auth(get("/api/v1/device/{id}",deviceId),adminToken),agentId,agentKey))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.outcome").value("DENIED"));

        mvc.perform(auth(put("/api/v1/agent/{id}/status",agentId),adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ACTIVE\"}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.agent_status").value("ACTIVE"));

        String techToken = user("TECHNICIAN");
        String homeToken = user("HOME_USER");

        mvc.perform(agent(auth(put("/api/v1/device/{id}",deviceId),techToken),agentId,agentKey)
                        .contentType(MediaType.APPLICATION_JSON).content(DEVICE_JSON))
                .andExpect(status().isOk());

        mvc.perform(agent(auth(post("/api/v1/agent/{id}/decisions",agentId),homeToken),agentId,agentKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"capability\":\"DEVICE_UPDATE\",\"target\":\"device/"+deviceId+"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outcome").value("DENIED"))
                .andExpect(jsonPath("$.requester_role").value("HOME_USER"));

        mvc.perform(agent(auth(post("/api/v1/agent/{id}/decisions",agentId),adminToken),agentId,agentKey)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"capability\":\"ALERT_RESOLVE\"}"))
                .andExpect(jsonPath("$.outcome").value("DENIED"));

        mvc.perform(agent(auth(put("/api/v1/device/{id}",deviceId),homeToken),agentId,agentKey)
                .contentType(MediaType.APPLICATION_JSON).content(DEVICE_JSON))
                        .andExpect(status().isForbidden());

        // leitura de alertas com o agente, em nome do morador
        mvc.perform(agent(auth(get("/api/v1/alert"),homeToken),agentId,agentKey))
                .andExpect(status().isOk());

        mvc.perform(agent(auth(get("/api/v1/device/{id}",deviceId),techToken),agentId,"errada"))
                .andExpect(status().isUnauthorized());
        mvc.perform(auth(get("/api/v1/device/{id}",deviceId),techToken).header("X-Agent-Id",agentId))
                .andExpect(status().isUnauthorized());

        mvc.perform(auth(get("/api/v1/device/{id}",deviceId),techToken))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/agent/{id}/heartbeat",agentId).header("X-Agent-Key",agentKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.communication_status").value("CONNECTED"));

        mvc.perform(post("/api/v1/agent/{id}/heartbeat",agentId).header("X-Agent-Key","errada"))
                .andExpect(status().isUnauthorized());

        mvc.perform(auth(put("/api/v1/agent/{id}/status",agentId),adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"SUSPENDED\"}"))
                .andExpect(status().isOk());
        mvc.perform(agent(auth(get("/api/v1/device/{id}",deviceId),techToken),agentId,agentKey))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("agent is SUSPENDED"));

        mvc.perform(auth(get("/api/v1/agent/{id}/actions",agentId),techToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()",greaterThanOrEqualTo(5)));
        mvc.perform(auth(get("/api/v1/agent/{id}/actions",agentId),homeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyAdminRegistersAgents()throws Exception{
        String techToken = user("TECHNICIAN");
        mvc.perform(auth(post("/api/v1/agent"),techToken).contentType(MediaType.APPLICATION_JSON).content("{\"agent_name\":\"x\",\"agent_type\":\"GENERAL_PURPOSE\","
                + "\"autonomy_level\":\"OBSERVER\",\"capabilities\":[]}")).andExpect(status().isForbidden());
    }
    //====================== UTILS ======================

    private String user(String role)throws Exception{
        String email = role.toLowerCase()+suffix()+"@agent.local";
        mvc.perform(auth(post("/api/v1/user"),adminToken).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"senha-forte-123\",\"role\":\"%s\"}".formatted(email,role)))
                .andExpect(status().isCreated());
        return token(email,"senha-forte-123");
    }

    private String token(String email,String password)throws Exception{
        return JsonPath.read(mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email,password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),"$.token");
    }
    private static MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder b, String token){
        return b.header(HttpHeaders.AUTHORIZATION,"Bearer "+token);
    }

    private static MockHttpServletRequestBuilder agent(MockHttpServletRequestBuilder b, String id,String key){
        return b.header("X-Agent-Id",id).header("X-Agent-Key",key);
    }

    private static String suffix(){return UUID.randomUUID().toString().substring(0,8);}

}