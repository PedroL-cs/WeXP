package com.wexp;

import com.wexp.feature.achievement.AchievementService;
import com.wexp.feature.achievement.IAchievementRepository;
import com.wexp.feature.auth.AuthService;
import com.wexp.feature.auth.JwtService;
import com.wexp.feature.auth.dto.RegisterRequestDto;
import com.wexp.feature.category.CategoryService;
import com.wexp.feature.category.ICategoryRepository;
import com.wexp.feature.game.GameService;
import com.wexp.feature.game.IGameRepository;
import com.wexp.feature.game.steam.ISteamGameRepository;
import com.wexp.feature.game.steam.SteamApiService;
import com.wexp.feature.genre.GenreService;
import com.wexp.feature.genre.IGenreRepository;
import com.wexp.feature.guide.GuideService;
import com.wexp.feature.guide.IGuideRepository;
import com.wexp.feature.guide.dto.CreateGuideRequestDto;
import com.wexp.feature.revision.GuideRevisionService;
import com.wexp.feature.revision.IGuideRevisionRepository;
import com.wexp.feature.user.IUserRepository;
import com.wexp.shared.client.steam.store.SteamStoreClient;
import com.wexp.shared.exception.ApiException;
import com.wexp.shared.exception.ExceptionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceBusinessRulesTests {

    @Mock
    private IUserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private IAchievementRepository achievementRepository;
    @Mock
    private IGameRepository gameRepository;
    @Mock
    private ICategoryRepository categoryRepository;
    @Mock
    private IGenreRepository genreRepository;
    @Mock
    private IGuideRepository guideRepository;
    @Mock
    private IGuideRevisionRepository revisionRepository;
    @Mock
    private ISteamGameRepository steamGameRepository;
    @Mock
    private SteamStoreClient steamStoreClient;
    @Mock
    private SteamApiService steamApiService;

    @Test
    void shouldRejectRegistrationWhenEmailIsAlreadyInUse() {
        AuthService authService = new AuthService(
                userRepository, passwordEncoder, jwtService, authenticationManager);
        RegisterRequestDto request = validRegistrationRequest();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new com.wexp.feature.user.UserEntity()));

        ApiException exception = assertThrows(ApiException.class, () -> authService.register(request));

        assertEquals(ExceptionResponse.EmailAlreadyInUse, exception.getExceptionResponse());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRegisterUserAndGenerateToken() {
        AuthService authService = new AuthService(
                userRepository, passwordEncoder, jwtService, authenticationManager);
        RegisterRequestDto request = validRegistrationRequest();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByLogin(request.getLogin())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(jwtService.generateToken(any(com.wexp.feature.user.UserEntity.class))).thenReturn("token");

        var response = authService.register(request);

        assertEquals("token", response.getToken());
        verify(userRepository).save(any(com.wexp.feature.user.UserEntity.class));
    }

    @Test
    void shouldRejectGuideCreationWhenGuideAlreadyExists() {
        GuideService guideService = new GuideService(guideRepository, achievementRepository);
        when(guideRepository.existsByAchievementPublicId("achievement-1")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () ->
                guideService.createInitialGuide(
                        "achievement-1",
                        new CreateGuideRequestDto("Guide content with enough characters"),
                        null));

        assertEquals(ExceptionResponse.GuideAlreadyExists, exception.getExceptionResponse());
        verifyNoInteractions(achievementRepository);
    }

    @Test
    void shouldRejectCategoryLookupWhenCategoryDoesNotExist() {
        CategoryService categoryService = new CategoryService(categoryRepository, gameRepository);
        when(categoryRepository.existsByPublicId("missing")).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, () ->
                categoryService.findGamesByCategory("missing", org.springframework.data.domain.Pageable.unpaged()));

        assertEquals(ExceptionResponse.CategoryNotFound, exception.getExceptionResponse());
        verifyNoInteractions(gameRepository);
    }

    @Test
    void shouldRejectGenreLookupWhenGenreDoesNotExist() {
        GenreService genreService = new GenreService(genreRepository, gameRepository);
        when(genreRepository.existsByPublicId("missing")).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, () ->
                genreService.findGamesByGenre("missing", org.springframework.data.domain.Pageable.unpaged()));

        assertEquals(ExceptionResponse.GenreNotFound, exception.getExceptionResponse());
        verifyNoInteractions(gameRepository);
    }

    @Test
    void shouldRejectAchievementLookupWhenAchievementDoesNotExist() {
        AchievementService achievementService = new AchievementService(gameRepository, achievementRepository);
        when(achievementRepository.findByPublicId("missing")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                achievementService.getAchievementDetails("missing"));

        assertEquals(ExceptionResponse.AchievementNotFound, exception.getExceptionResponse());
    }

    @Test
    void shouldRejectAchievementListWhenGameDoesNotExist() {
        AchievementService achievementService = new AchievementService(gameRepository, achievementRepository);
        when(gameRepository.findByPublicId("missing")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                achievementService.getAchievementList(
                        "missing", null, null, org.springframework.data.domain.Pageable.unpaged()));

        assertEquals(ExceptionResponse.GameNotFound, exception.getExceptionResponse());
        verifyNoInteractions(achievementRepository);
    }

    @Test
    void shouldRejectRevisionCreationWhenGuideDoesNotExist() {
        GuideRevisionService revisionService = new GuideRevisionService(guideRepository, revisionRepository);
        when(guideRepository.findByPublicId("missing")).thenReturn(Optional.empty());

        @SuppressWarnings("DataFlowIssue") ApiException exception = assertThrows(ApiException.class, () ->
                revisionService.createRevision("missing", null, null));

        assertEquals(ExceptionResponse.GuideNotFound, exception.getExceptionResponse());
        verifyNoInteractions(revisionRepository);
    }

    @Test
    void shouldRejectGameDetailsWhenGameDoesNotExist() {
        GameService gameService = new GameService(
                gameRepository, steamGameRepository, steamStoreClient, steamApiService);
        when(gameRepository.findByPublicId("missing")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                gameService.viewGameDetails("missing"));

        assertEquals(ExceptionResponse.GameNotFound, exception.getExceptionResponse());
    }

    private RegisterRequestDto validRegistrationRequest() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setLogin("test-user");
        request.setEmail("test@example.com");
        request.setUsername("Test User");
        request.setPassword("password123");
        request.setBirthDate(java.time.LocalDate.of(1990, 1, 1));
        return request;
    }
}
