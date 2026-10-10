package com.wexp.feature.game.steam;

import com.wexp.feature.game.GameEntity;
import com.wexp.feature.game.IGameRepository;
import com.wexp.shared.client.steam.SteamIntegrationException;
import com.wexp.shared.client.steam.store.SteamGameDetails;
import com.wexp.shared.client.steam.store.SteamStoreClient;
import com.wexp.shared.config.client.SteamClientProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.wexp.shared.client.steam.SteamIntegrationException.Reason.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameSynchronizationTests {
    @Mock private IGameRepository games;
    @Mock private SteamStoreClient store;
    @Mock private GameSteamPersistenceService persistence;
    private GameSynchronizationService service;
    private GameEntity local;

    @BeforeEach
    void setup() {
        SteamClientProperties properties = new SteamClientProperties();
        properties.setRefreshRetryDelay(Duration.ZERO);
        service = new GameSynchronizationService(games, store, persistence, properties);
        local = GameEntity.builder().publicId("game-1").steamAppId(10L).name("Preserved")
                .updatedAt(LocalDateTime.now()).steamSyncedAt(LocalDateTime.now().minusDays(8)).build();
    }

    @Test
    void shouldRetryTemporaryFailureOnlyUpToConfiguredLimit() {
        when(games.findByPublicId("game-1")).thenReturn(Optional.of(local));
        when(store.fetchGameDetails(10L)).thenThrow(new SteamIntegrationException(TIMEOUT));

        assertEquals(TIMEOUT, assertThrows(SteamIntegrationException.class, () -> service.synchronize("game-1")).getReason());

        verify(store, times(2)).fetchGameDetails(10L);
        verifyNoInteractions(persistence);
        assertEquals("Preserved", local.getName());
        assertTrue(local.getSteamSyncedAt().isBefore(LocalDateTime.now().minusDays(7)));
    }

    @Test
    void shouldPersistAfterRetrySucceeds() {
        SteamGameDetails details = new SteamGameDetails(10L, null, null);
        when(games.findByPublicId("game-1")).thenReturn(Optional.of(local));
        when(store.fetchGameDetails(10L)).thenThrow(new SteamIntegrationException(UNAVAILABLE))
                .thenReturn(Optional.of(details));

        service.synchronize("game-1");

        verify(persistence).refresh("game-1", details);
        verify(store, times(2)).fetchGameDetails(10L);
    }

    @Test
    void shouldNotRetryRateLimitOrMalformedResponse() {
        when(games.findByPublicId("game-1")).thenReturn(Optional.of(local));
        when(store.fetchGameDetails(10L)).thenThrow(new SteamIntegrationException(RATE_LIMITED, Duration.ofSeconds(60)));

        assertThrows(SteamIntegrationException.class, () -> service.synchronize("game-1"));
        verify(store).fetchGameDetails(10L);
        verifyNoInteractions(persistence);
    }

    @Test
    void shouldKeepExistingGameWhenSteamNoLongerReturnsIt() {
        when(games.findByPublicId("game-1")).thenReturn(Optional.of(local));
        when(store.fetchGameDetails(10L)).thenReturn(Optional.empty());

        service.synchronize("game-1");

        verifyNoInteractions(persistence);
        assertEquals("Preserved", local.getName());
    }

    @Test
    void shouldSkipFreshGamesAndLocalOnlyGames() {
        local.setSteamSyncedAt(LocalDateTime.now());
        service.refreshIfStale(local);
        local.setSteamSyncedAt(null);
        local.setSteamAppId(null);
        service.refreshIfStale(local);

        verifyNoInteractions(games, store, persistence);
    }

    @Test
    @Timeout(10)
    void shouldRunIndependentlyAndDeduplicateConcurrentRefreshes() throws Exception {
        CountDownLatch fetching = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch persisted = new CountDownLatch(1);
        SteamGameDetails details = new SteamGameDetails(10L, null, null);
        when(games.findByPublicId("game-1")).thenReturn(Optional.of(local));
        when(store.fetchGameDetails(10L)).thenAnswer(invocation -> {
            fetching.countDown();
            if (!release.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("test timeout");
            return Optional.of(details);
        });
        doAnswer(invocation -> { persisted.countDown(); return null; }).when(persistence).refresh("game-1", details);

        try {
            service.refreshIfStale(local);
            assertTrue(fetching.await(5, TimeUnit.SECONDS));
            service.refreshIfStale(local);
            verify(store).fetchGameDetails(10L);
            verifyNoInteractions(persistence);
            assertEquals("Preserved", local.getName());
        } finally {
            release.countDown();
        }
        assertTrue(persisted.await(5, TimeUnit.SECONDS));
    }
}
