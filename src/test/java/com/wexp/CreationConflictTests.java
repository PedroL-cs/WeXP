package com.wexp;

import com.wexp.feature.auth.AuthService;
import com.wexp.feature.auth.JwtService;
import com.wexp.feature.auth.dto.RegisterRequestDto;
import com.wexp.feature.game.GameEntity;
import com.wexp.feature.game.GameService;
import com.wexp.feature.game.IGameRepository;
import com.wexp.feature.game.dto.GameIdResponse;
import com.wexp.feature.game.steam.ISteamGameRepository;
import com.wexp.feature.game.steam.SteamApiService;
import com.wexp.feature.game.steam.GameSynchronizationService;
import com.wexp.feature.game.steam.GameSteamPersistenceService;
import com.wexp.shared.client.steam.store.SteamGameDetails;
import com.wexp.feature.user.IUserRepository;
import com.wexp.feature.user.UserEntity;
import com.wexp.shared.client.steam.store.SteamStoreClient;
import com.wexp.shared.exception.ApiException;
import com.wexp.shared.exception.ExceptionResponse;
import com.wexp.shared.persistence.PersistenceTransactions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreationConflictTests {

    @Mock private IUserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private IGameRepository gameRepository;
    @Mock private ISteamGameRepository steamGameRepository;
    @Mock private SteamStoreClient steamStoreClient;
    @Mock private SteamApiService steamApiService;
    @Mock private PersistenceTransactions persistenceTransactions;
    @Mock private GameSynchronizationService synchronizationService;
    @Mock private GameSteamPersistenceService steamPersistenceService;

    @BeforeEach
    void executeInsert() {
        lenient().when(persistenceTransactions.insert(any())).thenAnswer(invocation ->
                ((Supplier<?>) invocation.getArgument(0)).get());
    }

    @Test
    void shouldTranslateEmailRaceAfterFailedInsertWithoutIssuingToken() {
        RegisterRequestDto request = registration();
        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new UserEntity()));
        when(userRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("unique email"));

        ApiException exception = assertThrows(ApiException.class, () -> authService().register(request));

        assertEquals(ExceptionResponse.EmailAlreadyInUse, exception.getExceptionResponse());
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldTranslateLoginRaceAfterFailedInsertWithoutIssuingToken() {
        RegisterRequestDto request = registration();
        when(userRepository.findByLogin(request.getLogin()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new UserEntity()));
        when(userRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("unique login"));

        ApiException exception = assertThrows(ApiException.class, () -> authService().register(request));

        assertEquals(ExceptionResponse.UsernameAlreadyInUse, exception.getExceptionResponse());
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldNotMisclassifyUnrelatedRegistrationFailureAsDuplicate() {
        DataIntegrityViolationException failure = new DataIntegrityViolationException("unrelated constraint");
        when(userRepository.saveAndFlush(any())).thenThrow(failure);

        assertSame(failure, assertThrows(DataIntegrityViolationException.class,
                () -> authService().register(registration())));
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldReturnPersistedWinnerOnConcurrentGameShellCreation() {
        GameEntity winner = GameEntity.builder().publicId("game-winner").steamAppId(10L)
                .steamSyncedAt(LocalDateTime.now()).build();
        when(gameRepository.findBySteamAppId(10L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(winner));
        SteamGameDetails details = new SteamGameDetails(10L, null, null);
        when(steamStoreClient.fetchGameDetails(10L)).thenReturn(Optional.of(details));
        when(steamPersistenceService.create(anyString(), eq(details)))
                .thenThrow(new DataIntegrityViolationException("unique steam id"));

        GameIdResponse response = gameService().getGameId(10L);

        assertEquals("game-winner", response.gameId());
        assertEquals(GameIdResponse.GameStatus.NEW, response.gameStatus());
        verify(steamStoreClient).fetchGameDetails(10L);
    }

    @Test
    void shouldNotReturnUnpersistedGameIdWhenNoWinnerExists() {
        DataIntegrityViolationException failure = new DataIntegrityViolationException("unrelated constraint");
        SteamGameDetails details = new SteamGameDetails(10L, null, null);
        when(steamStoreClient.fetchGameDetails(10L)).thenReturn(Optional.of(details));
        when(steamPersistenceService.create(anyString(), eq(details))).thenThrow(failure);

        assertSame(failure, assertThrows(DataIntegrityViolationException.class, () -> gameService().getGameId(10L)));
        verify(steamStoreClient).fetchGameDetails(10L);
    }

    @Test
    void shouldReturnPersistedWinnerOnConcurrentSteamDetailCreation() {
        GameEntity winner = GameEntity.builder().id(1L).publicId("game-winner").steamAppId(10L)
                .steamSyncedAt(LocalDateTime.now()).build();
        when(gameRepository.findBySteamAppId(10L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(winner));
        SteamGameDetails details = new SteamGameDetails(10L, null, null);
        when(steamStoreClient.fetchGameDetails(10L)).thenReturn(Optional.of(details));
        when(steamPersistenceService.create(anyString(), eq(details)))
                .thenThrow(new DataIntegrityViolationException("unique steam id"));

        assertSame(winner, gameService().viewSteamGameDetails(10L));
        verify(gameRepository).incrementViewsCount(1L);
    }

    private AuthService authService() {
        return new AuthService(userRepository, passwordEncoder, jwtService, authenticationManager, persistenceTransactions);
    }

    private GameService gameService() {
        return new GameService(gameRepository, steamStoreClient, steamApiService,
                synchronizationService, steamPersistenceService);
    }

    private RegisterRequestDto registration() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("test@example.com");
        request.setLogin("test-user");
        request.setPassword("password123");
        return request;
    }
}
