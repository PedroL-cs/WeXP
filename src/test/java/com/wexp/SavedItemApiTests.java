package com.wexp;

import com.wexp.feature.achievement.AchievementEntity;
import com.wexp.feature.achievement.IAchievementRepository;
import com.wexp.feature.auth.JwtService;
import com.wexp.feature.game.GameEntity;
import com.wexp.feature.game.IGameRepository;
import com.wexp.feature.guide.GuideEntity;
import com.wexp.feature.guide.IGuideRepository;
import com.wexp.feature.library.SavedItemRepository;
import com.wexp.feature.user.IUserRepository;
import com.wexp.feature.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SavedItemApiTests {

    private static final String BASE_URL = "/api/v1/users/me/saved-items";

    @Autowired private MockMvc mvc;
    @Autowired private JwtService jwtService;
    @Autowired private IUserRepository users;
    @Autowired private IGameRepository games;
    @Autowired private IAchievementRepository achievements;
    @Autowired private IGuideRepository guides;
    @Autowired private SavedItemRepository savedItems;

    private UserEntity user;
    private UserEntity otherUser;
    private GameEntity game;
    private AchievementEntity achievement;
    private GuideEntity guide;

    @BeforeEach
    void setup() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        user = users.saveAndFlush(user("owner" + suffix));
        otherUser = users.saveAndFlush(user("other" + suffix));
        game = games.saveAndFlush(GameEntity.builder().name("Saved game " + suffix).build());
        achievement = achievements.saveAndFlush(AchievementEntity.builder()
                .game(game)
                .name("Saved achievement " + suffix)
                .isHidden(false)
                .build());
        guide = guides.saveAndFlush(GuideEntity.builder()
                .content("Conteúdo do guia salvo")
                .achievement(achievement)
                .author(user)
                .build());
    }

    @AfterEach
    void cleanup() {
        savedItems.deleteAll();
        guides.deleteAll();
        achievements.deleteAll();
        games.deleteAll();
        users.deleteAll();
    }

    @Test
    void shouldRequireAuthenticationForEverySavedItemEndpoint() throws Exception {
        mvc.perform(get(BASE_URL)).andExpect(status().isUnauthorized());
        mvc.perform(put(BASE_URL + "/games/{id}", game.getPublicId()))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete(BASE_URL + "/games/{id}", game.getPublicId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldSaveEachResourceIndependentlyAndListTheLibrary() throws Exception {
        String authorization = bearer(user);

        save("games", game.getPublicId(), "GAME", authorization);
        save("achievements", achievement.getPublicId(), "ACHIEVEMENT", authorization);
        save("guides", guide.getPublicId(), "GUIDE", authorization);
        save("games", game.getPublicId(), "GAME", authorization);

        mvc.perform(get(BASE_URL).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[*].type", containsInAnyOrder("GAME", "ACHIEVEMENT", "GUIDE")))
                .andExpect(jsonPath("$.content[*].resourceId", containsInAnyOrder(
                        game.getPublicId(), achievement.getPublicId(), guide.getPublicId()
                )));

        mvc.perform(get(BASE_URL)
                        .queryParam("type", "ACHIEVEMENT")
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].resourceId").value(achievement.getPublicId()));

        org.junit.jupiter.api.Assertions.assertEquals(3, savedItems.count());
    }

    @Test
    void shouldReportStatusAndRemoveIdempotently() throws Exception {
        String authorization = bearer(user);
        String url = BASE_URL + "/games/" + game.getPublicId();

        mvc.perform(get(url).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saved").value(false))
                .andExpect(jsonPath("$.savedAt").doesNotExist());

        mvc.perform(put(url).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saved").value(true))
                .andExpect(jsonPath("$.savedAt").isNotEmpty());

        mvc.perform(delete(url).header("Authorization", authorization))
                .andExpect(status().isNoContent());
        mvc.perform(delete(url).header("Authorization", authorization))
                .andExpect(status().isNoContent());

        mvc.perform(get(url).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saved").value(false));
    }

    @Test
    void shouldKeepSavedItemsPrivateToTheirOwner() throws Exception {
        save("guides", guide.getPublicId(), "GUIDE", bearer(user));

        mvc.perform(get(BASE_URL + "/guides/{id}", guide.getPublicId())
                        .header("Authorization", bearer(otherUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saved").value(false));

        mvc.perform(delete(BASE_URL + "/guides/{id}", guide.getPublicId())
                        .header("Authorization", bearer(otherUser)))
                .andExpect(status().isNoContent());

        mvc.perform(get(BASE_URL + "/guides/{id}", guide.getPublicId())
                        .header("Authorization", bearer(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saved").value(true));
    }

    @Test
    void shouldNotSaveRelatedResourcesImplicitly() throws Exception {
        save("guides", guide.getPublicId(), "GUIDE", bearer(user));

        mvc.perform(get(BASE_URL).header("Authorization", bearer(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].type").value("GUIDE"));

        mvc.perform(get(BASE_URL + "/games/{id}", game.getPublicId())
                        .header("Authorization", bearer(user)))
                .andExpect(jsonPath("$.saved").value(false));
        mvc.perform(get(BASE_URL + "/achievements/{id}", achievement.getPublicId())
                        .header("Authorization", bearer(user)))
                .andExpect(jsonPath("$.saved").value(false));
    }

    @Test
    void shouldPaginateAndRejectUnknownResources() throws Exception {
        String authorization = bearer(user);
        save("games", game.getPublicId(), "GAME", authorization);
        save("achievements", achievement.getPublicId(), "ACHIEVEMENT", authorization);
        save("guides", guide.getPublicId(), "GUIDE", authorization);

        mvc.perform(get(BASE_URL)
                        .queryParam("page", "0")
                        .queryParam("size", "2")
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(2));

        mvc.perform(put(BASE_URL + "/games/gam_missing").header("Authorization", authorization))
                .andExpect(status().isNotFound());
        mvc.perform(get(BASE_URL + "/guides/gui_missing").header("Authorization", authorization))
                .andExpect(status().isNotFound());
    }

    private void save(String resourcePath, String resourceId, String type, String authorization) throws Exception {
        mvc.perform(put(BASE_URL + "/" + resourcePath + "/" + resourceId)
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value(type))
                .andExpect(jsonPath("$.resourceId").value(resourceId))
                .andExpect(jsonPath("$.saved").value(true));
    }

    private String bearer(UserEntity authenticatedUser) {
        return "Bearer " + jwtService.generateToken(authenticatedUser);
    }

    private UserEntity user(String login) {
        return UserEntity.builder()
                .login(login)
                .email(login + "@example.com")
                .username("User " + login)
                .passwordHash("encoded-password")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
    }
}
