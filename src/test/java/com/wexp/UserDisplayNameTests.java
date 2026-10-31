package com.wexp;

import com.wexp.feature.auth.JwtService;
import com.wexp.feature.guide.GuideEntity;
import com.wexp.feature.guide.dto.GuideResponseDto;
import com.wexp.feature.revision.GuideRevisionEntity;
import com.wexp.feature.revision.dto.GuideRevisionResponse;
import com.wexp.feature.user.IUserRepository;
import com.wexp.feature.user.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:display_name_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"
})
@AutoConfigureMockMvc
class UserDisplayNameTests {
    @Autowired private MockMvc mvc;
    @Autowired private IUserRepository users;
    @Autowired private JwtService jwtService;

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void shouldKeepDisplayNameSeparateFromLoginDuringRegistrationAndProfileUpdates() throws Exception {
        String registered = mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login":"pedro_cs_regression",
                                  "username":"Pedro Silva",
                                  "email":"display-name-regression@example.com",
                                  "password":"senha-sintetica-123",
                                  "birthDate":"01/01/1990"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String token = json.readTree(registered).path("token").asString();
        UserEntity user = users.findByLogin("pedro_cs_regression").orElseThrow();
        String publicId = user.getPublicId();

        assertEquals("Pedro Silva", user.getDisplayName());
        assertEquals("pedro_cs_regression", user.getUsername());
        assertEquals("pedro_cs_regression", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));

        mvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("pedro_cs_regression"))
                .andExpect(jsonPath("$.username").value("Pedro Silva"));
        mvc.perform(get("/api/v1/users/{id}", publicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Pedro Silva"))
                .andExpect(jsonPath("$.login").doesNotExist());

        MockMultipartFile data = new MockMultipartFile("data", "data.json", MediaType.APPLICATION_JSON_VALUE,
                "{\"username\":\"Pedro Gamer\"}".getBytes(StandardCharsets.UTF_8));
        mvc.perform(multipart("/api/v1/users/me").file(data)
                        .with(request -> { request.setMethod("PATCH"); return request; })
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Pedro Gamer"))
                .andExpect(jsonPath("$.login").value("pedro_cs_regression"));

        UserEntity updated = users.findByLogin("pedro_cs_regression").orElseThrow();
        assertEquals("Pedro Gamer", updated.getDisplayName());
        assertEquals("pedro_cs_regression", updated.getUsername());
        assertTrue(jwtService.isTokenValid(token, updated));
        mvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Pedro Gamer"))
                .andExpect(jsonPath("$.login").value("pedro_cs_regression"));
        mvc.perform(get("/api/v1/users/{id}", publicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Pedro Gamer"));
        String loggedIn = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"pedro_cs_regression","password":"senha-sintetica-123"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertEquals("pedro_cs_regression", jwtService.extractUsername(json.readTree(loggedIn).path("token").asString()));
    }

    @Test
    void shouldPresentDisplayNameForLegacyGuideAuthor() {
        UserEntity creator = UserEntity.builder().publicId("usr_creator").login("creator_login")
                .username("Nome do criador").build();
        GuideEntity guide = GuideEntity.builder().publicId("gui_legacy").author(creator)
                .content("Conteúdo original").build();

        GuideResponseDto response = new GuideResponseDto(guide);
        assertEquals("Nome do criador", guide.getAuthorUsername());
        assertEquals("Nome do criador", response.author());
        assertEquals("Nome do criador", response.creator());
        assertEquals("creator_login", creator.getUsername());
    }

    @Test
    void shouldPresentDisplayNamesForGuideCreatorAndSelectedRevisionAuthor() {
        UserEntity creator = UserEntity.builder().publicId("usr_creator").login("creator_login")
                .username("Nome do criador").build();
        UserEntity reviewer = UserEntity.builder().publicId("usr_reviewer").login("reviewer_login")
                .username("Nome do revisor").build();
        GuideEntity guide = GuideEntity.builder().publicId("gui_reviewed").author(creator)
                .content("Conteúdo original").build();
        GuideRevisionEntity revision = GuideRevisionEntity.builder().publicId("rev_reviewed")
                .guide(guide).author(reviewer).content("Conteúdo revisado").build();
        guide.setActiveRevision(revision);

        GuideResponseDto response = new GuideResponseDto(guide);
        GuideRevisionResponse revisionResponse = new GuideRevisionResponse(revision);
        assertEquals("Nome do revisor", response.author());
        assertEquals("Nome do criador", response.creator());
        assertEquals("usr_reviewer", response.authorPublicId());
        assertEquals("usr_creator", response.creatorPublicId());
        assertEquals("Nome do revisor", revisionResponse.getAuthor());
        assertEquals("reviewer_login", reviewer.getUsername());
    }
}
