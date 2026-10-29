package com.wexp;

import com.wexp.feature.game.*;
import com.wexp.feature.game.steam.*;
import com.wexp.shared.client.steam.SteamIntegrationException;
import com.wexp.shared.client.steam.store.SteamGameDetails;
import com.wexp.shared.client.steam.store.SteamStoreClient;
import com.wexp.shared.persistence.PersistenceTransactions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameAvailabilityTests {
    @Mock private IGameRepository games;
    @Mock private ISteamGameRepository steamGames;
    @Mock private SteamStoreClient store;
    @Mock private SteamApiService api;
    @Mock private PersistenceTransactions transactions;
    @Mock private GameSynchronizationService synchronization;
    @Mock private GameSteamPersistenceService persistence;
    private GameService service;
    private GameEntity local;

    @BeforeEach
    void setup() {
        service = new GameService(games, store, api, synchronization, persistence);
        local = GameEntity.builder().id(1L).publicId("game-1").steamAppId(10L).name("Local data")
                .steamSyncedAt(LocalDateTime.now().minusDays(8)).build();
    }

    @Test
    void shouldReturnLocalGameWithoutCallingSteam() {
        when(games.findByPublicId("game-1")).thenReturn(Optional.of(local));

        assertSame(local, service.viewGameDetails("game-1"));

        verify(synchronization).refreshIfStale(local);
        verify(games).incrementViewsCount(1L);
        verifyNoInteractions(store, api, persistence);
    }

    @Test
    void shouldReturnLocalSteamGameWithoutCallingSteam() {
        when(games.findBySteamAppId(10L)).thenReturn(Optional.of(local));

        assertSame(local, service.viewSteamGameDetails(10L));
        verify(synchronization).refreshIfStale(local);
        verifyNoInteractions(store, api, persistence);
    }

    @Test
    void shouldMaterializeNewGameBeforeReturningItsId() {
        SteamGameDetails details = new SteamGameDetails(10L, null, null);
        GameEntity materialized = GameEntity.builder()
                .id(2L)
                .publicId("game-new")
                .steamAppId(10L)
                .steamSyncedAt(LocalDateTime.now())
                .build();
        when(games.findBySteamAppId(10L)).thenReturn(Optional.empty());
        when(store.fetchGameDetails(10L)).thenReturn(Optional.of(details));
        when(persistence.create(anyString(), eq(details))).thenReturn(materialized);

        var response = service.getGameId(10L);

        assertEquals("game-new", response.gameId());
        assertEquals(com.wexp.feature.game.dto.GameIdResponse.GameStatus.NEW, response.gameStatus());
        verifyNoInteractions(synchronization);
    }

    @Test
    void shouldMaterializeLegacyShellBeforeReturningItsId() {
        GameEntity shell = GameEntity.builder().publicId("game-shell").steamAppId(10L).build();
        SteamGameDetails details = new SteamGameDetails(10L, null, null);
        GameEntity materialized = GameEntity.builder()
                .publicId("game-shell")
                .steamAppId(10L)
                .steamSyncedAt(LocalDateTime.now())
                .build();
        when(games.findBySteamAppId(10L)).thenReturn(Optional.of(shell));
        when(store.fetchGameDetails(10L)).thenReturn(Optional.of(details));
        when(persistence.refresh("game-shell", details)).thenReturn(materialized);

        var response = service.getGameId(10L);

        assertEquals("game-shell", response.gameId());
        assertEquals(com.wexp.feature.game.dto.GameIdResponse.GameStatus.EXISTING, response.gameStatus());
        verifyNoInteractions(synchronization);
    }

    @Test
    void shouldSearchLocalDataEvenWhenSteamIsUnavailable() {
        var page = PageRequest.of(0, 20);
        when(games.searchByNameOrDescription("Local", page)).thenReturn(new PageImpl<>(List.of(local), page, 1));

        assertEquals(List.of(local), service.searchGames("Local", page).getContent());
        verify(synchronization).refreshIfStale(local);
        verifyNoInteractions(store, api, persistence);
    }

    @Test
    void shouldPropagateTemporaryFailureForGameWithoutLocalData() {
        when(store.fetchGameDetails(10L)).thenThrow(new SteamIntegrationException(SteamIntegrationException.Reason.TIMEOUT));

        SteamIntegrationException error = assertThrows(SteamIntegrationException.class,
                () -> service.viewSteamGameDetails(10L));
        assertEquals(SteamIntegrationException.Reason.TIMEOUT, error.getReason());
        verifyNoInteractions(persistence);
        verify(games, never()).incrementViewsCount(anyLong());
    }
}
