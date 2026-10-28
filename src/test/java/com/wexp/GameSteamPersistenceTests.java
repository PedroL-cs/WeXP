package com.wexp;

import com.wexp.feature.achievement.AchievementEntity;
import com.wexp.feature.game.GameEntity;
import com.wexp.feature.game.IGameRepository;
import com.wexp.feature.game.steam.GameSteamPersistenceService;
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
import java.util.Map;

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
    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void shouldPersistSynchronizationTimeAndRemoteImageUrls() {
        var transaction = new TransactionTemplate(transactionManager);
        GameEntity game = transaction.execute(status -> {
            GameEntity saved = persistence.create("game-sync-1", details("""
                    {"name":"Game","header_image":"https://steam.example/header.jpg"}
                    """));
            assertNotNull(saved.getSteamSyncedAt());
            verifyNoInteractions(remoteStore, remoteApi, requests);
            return saved;
        });

        assertNotNull(game);
        GameEntity persisted = games.findByPublicId(game.getPublicId()).orElseThrow();
        assertNotNull(persisted.getSteamSyncedAt());
        assertEquals("https://steam.example/header.jpg", persisted.getHeaderImageUrl());
        assertTrue(persisted.getHeroImageUrl().endsWith("/10/library_hero.jpg"));
    }

    @Test
    void shouldPersistAchievementIconUrl() {
        var schema = json.readTree("""
                {"game":{"availableGameStats":{"achievements":[{
                  "name":"ACH_ONE","displayName":"First","description":"Description",
                  "hidden":0,"icon":"https://steam.example/achievement.jpg"
                }]}}}
                """);

        AchievementEntity achievement = new TransactionTemplate(transactionManager).execute(status -> {
            GameEntity game = persistence.create("game-sync-icon", new SteamGameDetails(
                    13L,
                    json.readTree("{\"name\":\"Game\"}"),
                    schema,
                    Map.of("ACH_ONE", 2.5)
            ));
            return game.getAchievements().get(0);
        });

        assertNotNull(achievement);
        assertEquals("https://steam.example/achievement.jpg", achievement.getIconUrl());
        assertEquals("ACH_ONE", achievement.getSteamApiName());
        assertEquals(2.5, achievement.getGlobalUnlockPercentage());
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
        verifyNoInteractions(remoteStore, remoteApi, requests);
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
