package com.wexp;

import com.wexp.feature.achievement.AchievementController;
import com.wexp.feature.achievement.AchievementService;
import com.wexp.feature.achievement.dto.AchievementResponseDto;
import com.wexp.feature.auth.AuthController;
import com.wexp.feature.auth.AuthService;
import com.wexp.feature.auth.JwtService;
import com.wexp.feature.auth.dto.LoginResponseDto;
import com.wexp.feature.category.CategoryController;
import com.wexp.feature.category.CategoryService;
import com.wexp.feature.game.GameController;
import com.wexp.feature.game.GameEntity;
import com.wexp.feature.game.GameService;
import com.wexp.feature.game.dto.GameIdResponse;
import com.wexp.feature.game.steam.SteamApiService;
import com.wexp.feature.game.steam.SteamGameEntity;
import com.wexp.feature.genre.GenreController;
import com.wexp.feature.genre.GenreService;
import com.wexp.feature.guide.GuideController;
import com.wexp.feature.guide.GuideEntity;
import com.wexp.feature.guide.GuideService;
import com.wexp.feature.image.ImageController;
import com.wexp.feature.image.ImageEntity;
import com.wexp.feature.image.ImageService;
import com.wexp.feature.revision.GuideRevisionController;
import com.wexp.feature.revision.GuideRevisionService;
import com.wexp.feature.revision.dto.GuideRevisionResponse;
import com.wexp.feature.user.UserController;
import com.wexp.feature.user.UserEntity;
import com.wexp.feature.user.UserService;
import com.wexp.feature.user.dto.UserPublicResponseDto;
import com.wexp.feature.user.dto.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {
        AuthController.class,
        UserController.class,
        GameController.class,
        AchievementController.class,
        CategoryController.class,
        GenreController.class,
        GuideController.class,
        GuideRevisionController.class,
        ImageController.class
})
@AutoConfigureMockMvc(addFilters = false)
class ApiControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private GameService gameService;
    @MockitoBean
    private SteamApiService steamApiService;
    @MockitoBean
    private AchievementService achievementService;
    @MockitoBean
    private CategoryService categoryService;
    @MockitoBean
    private GenreService genreService;
    @MockitoBean
    private GuideService guideService;
    @MockitoBean
    private GuideRevisionService guideRevisionService;
    @MockitoBean
    private ImageService imageService;

    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponseDto("token-teste"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"usuario","password":"senha-segura"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-teste"))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void shouldRejectLoginWithMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors").isArray());

        verifyNoInteractions(authService);
    }

    @Test
    void shouldRegisterUserWithValidPayload() throws Exception {
        when(authService.register(any())).thenReturn(new LoginResponseDto("token-cadastro"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login":"novo_usuario",
                                  "email":"novo@exemplo.com",
                                  "username":"Novo Usuário",
                                  "password":"senha-segura",
                                  "birthDate":"01/01/1990"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token-cadastro"));
    }

    @Test
    void shouldRejectRegistrationWithInvalidEmailAndBirthDate() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login":"ab",
                                  "email":"invalido",
                                  "username":"",
                                  "password":"123",
                                  "birthDate":"01/01/2030"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());

        verifyNoInteractions(authService);
    }

    @Test
    void shouldReturnCurrentAndPublicUserProfiles() throws Exception {
        UserResponseDto current = UserResponseDto.builder()
                .publicId("usr-1").login("usuario").email("usuario@exemplo.com")
                .username("Usuário").build();
        UserPublicResponseDto publicProfile = UserPublicResponseDto.builder()
                .publicId("usr-2").username("Outro usuário").build();
        when(userService.getCurrentUser(nullable(UserEntity.class))).thenReturn(current);
        when(userService.getPublicProfile("usr-2")).thenReturn(publicProfile);

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value("usr-1"));

        mockMvc.perform(get("/api/v1/users/usr-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Outro usuário"));
    }

    @Test
    void shouldUpdateProfileWithMultipartRequest() throws Exception {
        when(userService.updateCurrentUser(nullable(UserEntity.class), any(), any()))
                .thenReturn(UserResponseDto.builder().publicId("usr-1").username("Atualizado").build());

        mockMvc.perform(multipart("/api/v1/users/me")
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .file("avatar", "imagem".getBytes())
                        .param("data", "{\"username\":\"Atualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Atualizado"));
    }

    @Test
    void shouldSearchGamesWithPagination() throws Exception {
        GameEntity game = GameEntity.builder()
                .publicId("game-1").name("Jogo teste").steamAppId(10L).build();
        when(gameService.searchGames(eq("teste"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(game)));
        when(gameService.getMostViewedGames(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(game)));
        when(gameService.getMostRecentGames(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(game)));

        mockMvc.perform(get("/api/v1/games")
                        .param("q", "teste")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Jogo teste"))
                .andExpect(jsonPath("$.page").value(0));

        mockMvc.perform(get("/api/v1/games/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Jogo teste"));

        mockMvc.perform(get("/api/v1/games/released"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Jogo teste"));
    }

    @Test
    void shouldRejectInvalidSortBeforeCallingService() throws Exception {
        mockMvc.perform(get("/api/v1/games")
                        .param("q", "teste")
                        .param("sort", "campoInexistente,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("ordenação")));

        verifyNoInteractions(gameService);
    }

    @Test
    void shouldRejectInvalidPageParameter() throws Exception {
        mockMvc.perform(get("/api/v1/games")
                        .param("q", "teste")
                        .param("page", "texto"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnGameDetailsAndId() throws Exception {
        GameEntity game = GameEntity.builder()
                .publicId("game-1").name("Jogo teste").steamAppId(10L).build();
        when(gameService.viewGameDetails("game-1")).thenReturn(game);
        when(gameService.viewSteamGameDetails(10L)).thenReturn(game);
        when(gameService.getGameId(10L)).thenReturn(GameIdResponse.builder()
                .steamId(10L)
                .gameId("game-1")
                .gameStatus(GameIdResponse.GameStatus.EXISTING)
                .build());

        mockMvc.perform(get("/api/v1/games/game-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jogo teste"));
        mockMvc.perform(get("/api/v1/games/steam/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.steamAppId").value(10));
        mockMvc.perform(get("/api/v1/games/steam/10/id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("game-1"));
    }

    @Test
    void shouldSearchSteamGamesWithPagination() throws Exception {
        SteamGameEntity game = SteamGameEntity.builder().appid(10L).name("Jogo Steam").build();
        when(steamApiService.instantSearchGame(eq("steam"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(game)));

        mockMvc.perform(get("/api/v1/games/steam-games")
                        .param("q", "steam")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Jogo Steam"));
    }

    @Test
    void shouldReturnAchievementsAndFilterByGame() throws Exception {
        when(achievementService.getAchievementDetails("ach-1"))
                .thenReturn(mock(AchievementResponseDto.class));
        when(achievementService.searchAchievement(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(achievementService.getAchievementList(eq("game-1"), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/achievements/ach-1"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/achievements").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        mockMvc.perform(get("/api/v1/games/game-1/achievements"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnCategoriesAndGenres() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of());
        when(genreService.findAll()).thenReturn(List.of());
        when(categoryService.findGamesByCategory(eq("cat-1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(genreService.findGamesByGenre(eq("genre-1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        mockMvc.perform(get("/api/v1/categories/cat-1/games"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        mockMvc.perform(get("/api/v1/genres/genre-1/games"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnAndCreateGuide() throws Exception {
        GuideEntity guide = mock(GuideEntity.class);
        when(guide.getPublicId()).thenReturn("guide-1");
        when(guide.getContent()).thenReturn("Conteúdo suficientemente longo");
        when(guide.getVersion()).thenReturn(1);
        when(guide.getAchievementPublicId()).thenReturn("ach-1");
        when(guide.getAuthorUsername()).thenReturn("usuario");
        when(guideService.getGuideByAchievementPublicId("ach-1")).thenReturn(guide);
        when(guideService.getGuideByPublicId("guide-1")).thenReturn(guide);
        when(guideService.createInitialGuide(eq("ach-1"), any(), nullable(UserEntity.class))).thenReturn(guide);

        mockMvc.perform(get("/api/v1/achievements/ach-1/guide"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("guide-1"));
        mockMvc.perform(get("/api/v1/guides/guide-1"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/achievements/ach-1/guide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"Conteúdo suficientemente longo"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectGuideWithInsufficientContent() throws Exception {
        mockMvc.perform(post("/api/v1/achievements/ach-1/guide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"curto\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(guideService);
    }

    @Test
    void shouldCreateGuideRevision() throws Exception {
        when(guideRevisionService.createRevision(eq("guide-1"), any(), nullable(UserEntity.class)))
                .thenReturn(GuideRevisionResponse.builder()
                        .publicId("revision-1").guidePublicId("guide-1")
                        .content("conteúdo revisado").build());

        mockMvc.perform(post("/api/v1/guides/guide-1/revisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"conteúdo revisado","changeSummary":"Correção de texto"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").value("revision-1"));
    }

    @Test
    void shouldUploadDownloadAndDeleteImage() throws Exception {
        ImageEntity image = mock(ImageEntity.class);
        when(image.getPublicId()).thenReturn("img-1");
        when(image.getOwnerType()).thenReturn("user");
        when(image.getOwnerId()).thenReturn("usr-1");
        when(image.getVariant()).thenReturn("profile");
        when(imageService.replace(anyString(), anyString(), anyString(), any()))
                .thenReturn(image);
        when(imageService.get("img-1"))
                .thenReturn(ResponseEntity.ok(new ByteArrayResource("imagem".getBytes())));
        doNothing().when(imageService).delete("img-1");

        mockMvc.perform(multipart("/api/v1/images")
                        .file("file", "imagem".getBytes())
                        .param("ownerType", "user")
                        .param("ownerId", "usr-1")
                        .param("variant", "profile"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("img-1"));
        mockMvc.perform(get("/api/v1/images/img-1"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/images/img-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundForUnknownRoute() throws Exception {
        mockMvc.perform(get("/api/v1/rota-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }
}
