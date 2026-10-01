package com.itcinnovation.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSecurityErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedApiRequestsReturnJsonInsteadOfHtml() throws Exception {
        mockMvc.perform(get("/api/reports/mine"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Session expirée ou authentification requise."));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void forbiddenApiRequestsReturnJsonInsteadOfHtml() throws Exception {
        mockMvc.perform(get("/api/reports/manager"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Vous n’êtes pas autorisé à effectuer cette action."));
    }
}