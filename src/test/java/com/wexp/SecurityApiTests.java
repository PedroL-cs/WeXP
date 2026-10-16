package com.wexp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRequireAuthenticationForCurrentProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowReadingGuidesAndRequireAuthenticationForChanges() throws Exception {
        mockMvc.perform(get("/api/v1/guides/guide-1"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/achievements/ach-1/guide"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/guides/guide-1/revisions"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/guides/guide-1/revisions")
                        .contentType("application/json")
                        .content("{\"content\":\"conteudo\",\"changeSummary\":\"resumo\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldTreatInvalidTokenAsAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/guides/guide-1")
                        .header(AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/users/me")
                        .header(AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }
}
