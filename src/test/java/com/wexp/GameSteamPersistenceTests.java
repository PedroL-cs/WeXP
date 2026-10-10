package com.wexp;

import com.wexp.feature.game.GameEntity;
import com.wexp.feature.game.IGameRepository;
import com.wexp.feature.game.steam.GameSteamPersistenceService;
import com.wexp.feature.image.ImageService;
import com.wexp.shared.client.steam.SteamRequestExecutor;
import com.wexp.shared.client.steam.api.SteamApiClient;
import com.wexp.shared.client.steam.store.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"}, showSql = false)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({GameSteamPersistenceService.class, SteamStoreClient.class})
class GameSteamPersistenceTests {
    @Autowired private GameSteamPersistenceService persistence;
    @Autowired private IGameRepository games;
    @Autowired private PlatformTransactionManager transactionManager;
    @MockitoBean private SteamStore remoteStore;
    @MockitoBean private SteamApiClient remoteApi;
    @MockitoBean private SteamRequestExecutor requests;
    @MockitoBean private ImageService images;
    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void shouldPersistSynchronizationTimeAndScheduleImagesOnlyAfterCommit() {
        var transaction = new TransactionTemplate(transactionManager);
        GameEntity game = transaction.execute(status -> {
            GameEntity saved = persistence.create("game-sync-1", details("{\"name\":\"Game\"}"));
            assertNotNull(saved.getSteamSyncedAt());
            verifyNoInteractions(images, remoteStore, remoteApi, requests);
            return saved;
        });

        assertNotNull(game);
        assertNotNull(games.findByPublicId(game.getPublicId()).orElseThrow().getSteamSyncedAt());
        verify(images).replaceFromUrlsAsync(eq("game"), eq(game.getPublicId()), anyMap());
    }

    @Test
    void shouldRollBackMetadataAndSynchronizationTimeWithoutSchedulingImages() {
        GameEntity original = games.saveAndFlush(GameEntity.builder().publicId("game-sync-2").steamAppId(11L)
                .name("Original").steamSyncedAt(LocalDateTime.now().minusDays(8)).build());
        LocalDateTime previousSync = games.findByPublicId(original.getPublicId()).orElseThrow().getSteamSyncedAt();
        String response = "{\"name\":\"New name\",\"genres\":[{\"id\":\"test\",\"description\":\"" + "x".repeat(2000) + "\"}]}";

        assertThrows(DataIntegrityViolationException.class, () -> persistence.refresh(original.getPublicId(), details(response)));

        GameEntity current = games.findByPublicId(original.getPublicId()).orElseThrow();
        assertEquals("Original", current.getName());
        assertEquals(previousSync, current.getSteamSyncedAt());
        verifyNoInteractions(images, remoteStore, remoteApi, requests);
    }

    @Test
    void shouldNotOverwriteViewsIncrementedAfterLoadingGame() {
        GameEntity original = games.saveAndFlush(GameEntity.builder().publicId("game-sync-3").steamAppId(12L)
                .name("Original").build());
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            games.findByPublicId(original.getPublicId()).orElseThrow();
            games.incrementViewsCount(original.getId());
            persistence.refresh(original.getPublicId(), details("{\"name\":\"New name\"}"));
        });

        GameEntity current = games.findByPublicId(original.getPublicId()).orElseThrow();
        assertEquals(1L, current.getViewsCount());
        assertEquals("New name", current.getName());
    }

    private SteamGameDetails details(String data) {
        return new SteamGameDetails(10L, json.readTree(data), null);
    }
}
