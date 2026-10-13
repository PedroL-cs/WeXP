package com.wexp;

import com.wexp.feature.achievement.*;
import com.wexp.feature.auth.JwtService;
import com.wexp.feature.game.*;
import com.wexp.feature.guide.GuideService;
import com.wexp.feature.guide.dto.*;
import com.wexp.feature.revision.GuideRevisionService;
import com.wexp.feature.revision.dto.*;
import com.wexp.feature.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:revision_voting_api;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false", "spring.jpa.open-in-view=false"
})
@AutoConfigureMockMvc
class RevisionVotingApiTests {
    @Autowired private MockMvc mvc;
    @Autowired private JwtService jwt;
    @Autowired private IUserRepository users;
    @Autowired private IGameRepository games;
    @Autowired private IAchievementRepository achievements;
    @Autowired private GuideService guides;
    @Autowired private GuideRevisionService revisions;

    private UserEntity creator;
    private UserEntity editor;
    private UserEntity voter;
    private AchievementEntity achievement;
    private GuideResponseDto guide;
    private GuideRevisionResponse revision;
    private final JsonMapper json = JsonMapper.builder().build();

    @BeforeEach
    void setup() {
        creator = user();
        editor = user();
        voter = user();
        GameEntity game = games.saveAndFlush(GameEntity.builder().name("Game").build());
        achievement = achievements.saveAndFlush(AchievementEntity.builder().game(game).name("Achievement").isHidden(false).build());
        guide = guides.createInitialGuide(achievement.getPublicId(), new CreateGuideRequestDto("Conteúdo original do guia"), creator);
        revision = revisions.createRevision(guide.id(), request(guide.originalRevisionId()), editor);
    }

    @Test
    void shouldKeepReadingPublicAndRequireAuthenticationForChanges() throws Exception {
        mvc.perform(get("/api/v1/achievements/{id}/revisions", achievement.getPublicId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(get("/api/v1/revisions/{id}", guide.originalRevisionId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isMain").value(true))
                .andExpect(jsonPath("$.isOriginal").value(true))
                .andExpect(jsonPath("$.main").doesNotExist())
                .andExpect(jsonPath("$.viewerHasVoted").value(false));
        mvc.perform(put("/api/v1/revisions/{id}/upvote", revision.getPublicId()))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/v1/revisions/{id}/upvote", revision.getPublicId()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/guides/{id}/revisions", guide.id()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request(guide.originalRevisionId()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldExposeWinnerAuthorshipAndViewerVoteState() throws Exception {
        String token = bearer(voter);
        mvc.perform(put("/api/v1/revisions/{id}/upvote", revision.getPublicId()).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteCount").value(1))
                .andExpect(jsonPath("$.viewerHasVoted").value(true))
                .andExpect(jsonPath("$.isMain").value(true))
                .andExpect(jsonPath("$.activeRevisionId").value(revision.getPublicId()));
        mvc.perform(get("/api/v1/achievements/{id}/guide", achievement.getPublicId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guide.id()))
                .andExpect(jsonPath("$.content").value(revision.getContent()))
                .andExpect(jsonPath("$.authorPublicId").value(editor.getPublicId()))
                .andExpect(jsonPath("$.creatorPublicId").value(creator.getPublicId()));
        mvc.perform(get("/api/v1/revisions/{id}", revision.getPublicId()).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewerHasVoted").value(true))
                .andExpect(jsonPath("$.isMain").value(true))
                .andExpect(jsonPath("$.isOriginal").value(false))
                .andExpect(jsonPath("$.baseRevisionId").value(guide.originalRevisionId()));
        mvc.perform(delete("/api/v1/revisions/{id}/upvote", revision.getPublicId()).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewerHasVoted").value(false))
                .andExpect(jsonPath("$.voteCount").value(0));
    }

    @Test
    void shouldRejectAuthorVotesForOriginalAndRevision() throws Exception {
        mvc.perform(put("/api/v1/revisions/{id}/upvote", guide.originalRevisionId()).header("Authorization", bearer(creator)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(24));
        mvc.perform(put("/api/v1/revisions/{id}/upvote", revision.getPublicId()).header("Authorization", bearer(editor)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(24));
    }

    @Test
    void shouldRequireExplicitBaseAndAcceptBaseThatLostLeadership() throws Exception {
        mvc.perform(post("/api/v1/guides/{id}/revisions", guide.id()).header("Authorization", bearer(editor))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Conteúdo suficientemente longo\",\"changeSummary\":\"Resumo\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("baseRevisionId"));
        mvc.perform(put("/api/v1/revisions/{id}/upvote", revision.getPublicId()).header("Authorization", bearer(voter)))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/guides/{id}/revisions", guide.id()).header("Authorization", bearer(creator))
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request(guide.originalRevisionId()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.baseRevisionId").value(guide.originalRevisionId()))
                .andExpect(jsonPath("$.isBasedOnCurrentMain").value(false))
                .andExpect(jsonPath("$.voteCount").value(0));
    }

    @Test
    void shouldReturnPaginatedRevisionsForGuideAndAchievement() throws Exception {
        mvc.perform(get("/api/v1/guides/{id}/revisions", guide.id()).param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalPages").value(2));
        mvc.perform(get("/api/v1/achievements/{id}/revisions", achievement.getPublicId()).param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].publicId").value(revision.getPublicId()));
        mvc.perform(get("/api/v1/guides/{id}/revisions", guide.id()).param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    private String bearer(UserEntity user) {
        return "Bearer " + jwt.generateToken(user);
    }

    private CreateGuideRevisionRequest request(String base) {
        CreateGuideRevisionRequest request = new CreateGuideRevisionRequest();
        request.setBaseRevisionId(base);
        request.setContent("Conteúdo completo da revisão publicada");
        request.setChangeSummary("Correção de informações");
        return request;
    }

    private UserEntity user() {
        String login = "u" + UUID.randomUUID().toString().substring(0, 20);
        return users.saveAndFlush(UserEntity.builder().login(login).email(login + "@example.com")
                .username("Usuário de teste").passwordHash("encoded").birthDate(LocalDate.of(1990, 1, 1)).build());
    }
}
