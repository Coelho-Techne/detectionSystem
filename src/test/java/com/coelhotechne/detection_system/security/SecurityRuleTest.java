package com.coelhotechne.detection_system.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
class SecurityRuleTest {

    @Autowired
    MockMvc mvc;

    @Test
    void cameraAuthIsPublic()throws Exception{
        mvc.perform(post("/api/v1/cam/{camId}/auth",UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"access_key\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deviceAuthIsPublic() throws Exception {
        mvc.perform(post("/api/v1/device/{id}/authenticate", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"access_key\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void anonymousIsRejected()throws Exception{
        mvc.perform(get("/api/v1/zone")).andExpect(status().isUnauthorized());
    }
    @Test
    void healthIsPublic()throws Exception{
        mvc.perform(get("/actuator/health")).andExpect(status().is2xxSuccessful());
    }

    @Test
    @WithMockUser(roles = "HOME_USER")
    void homologationForbiddenForHomeUser() throws Exception {
        mvc.perform(post("/api/v1/cam/{camId}/homologation/test", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void homologationAllowedForTechnician() throws Exception {
        mvc.perform(post("/api/v1/cam/{camId}/homologation/test", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void cameraApprovalIsAdminOnly()throws Exception{
        mvc.perform(post("/api/v1/cam/{camId}/homologation/approve",UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void technicianCanWriteLocation()throws Exception{
        mvc.perform(post("/api/v1/location")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"x\",\"latitude\":1,\"longitude\":1,\"zone_uuid\":\"%s\"}"
                        .formatted(UUID.randomUUID()))).andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(roles = "HOME_USER")
    void homeUserCannotDeleteTelemetry()throws Exception{
        mvc.perform(delete("/api/v1/telemetry/{id}",UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
    @Test
    @WithMockUser(roles = "AI_USER")
    void aiUserCannotManageUsers()throws Exception{
        mvc.perform(get("/api/v1/user")).andExpect(status().isForbidden());
    }


}