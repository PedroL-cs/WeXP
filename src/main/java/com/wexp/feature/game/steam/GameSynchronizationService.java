package com.wexp.feature.game.steam;

import com.wexp.feature.game.GameEntity;
import com.wexp.feature.game.IGameRepository;
import com.wexp.shared.client.steam.SteamIntegrationException;
import com.wexp.shared.client.steam.store.SteamGameDetails;
import com.wexp.shared.client.steam.store.SteamStoreClient;
import com.wexp.shared.config.client.SteamClientProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GameSynchronizationService {
    private static final Logger logger = LoggerFactory.getLogger(GameSynchronizationService.class);
    private final IGameRepository gameRepository;
    private final SteamStoreClient steamStoreClient;
    private final GameSteamPersistenceService persistence;
    private final SteamClientProperties properties;
    private final Set<String> running = ConcurrentHashMap.newKeySet();

    public void refreshIfStale(GameEntity game) {
        if (game.getSteamAppId() == null || !needsRefresh(game) || !running.add(game.getPublicId())) return;
        String publicId = game.getPublicId();
        try {
            CompletableFuture.runAsync(() -> {
                try {
                    synchronize(publicId);
                } catch (SteamIntegrationException exception) {
                    logger.warn("Sincronização Steam adiada para {}: {}", publicId, exception.getReason());
                } catch (RuntimeException exception) {
                    logger.error("Erro ao persistir sincronização do jogo {}", publicId, exception);
                } finally {
                    running.remove(publicId);
                }
            });
        } catch (RuntimeException exception) {
            running.remove(publicId);
            logger.warn("Não foi possível iniciar a sincronização do jogo {}", publicId);
        }
    }

    void synchronize(String publicId) {
        GameEntity current = gameRepository.findByPublicId(publicId).orElse(null);
        if (current == null || current.getSteamAppId() == null) return;
        if (!needsRefresh(current)) return;
        for (int attempt = 1; attempt <= properties.getRefreshMaxAttempts(); attempt++) {
            try {
                Optional<SteamGameDetails> details = steamStoreClient.fetchGameDetails(current.getSteamAppId());
                details.ifPresent(value -> persistence.refresh(publicId, value));
                return;
            } catch (SteamIntegrationException exception) {
                if (!exception.isRetryable() || attempt == properties.getRefreshMaxAttempts()) throw exception;
                try {
                    Thread.sleep(properties.getRefreshRetryDelay().toMillis());
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private boolean isStale(GameEntity game) {
        return game.getSteamSyncedAt() == null
                || game.getSteamSyncedAt().isBefore(LocalDateTime.now().minus(properties.getRefreshInterval()));
    }

    private boolean needsRefresh(GameEntity game) {
        return isStale(game) || game.getAchievementPercentagesCheckedAt() == null;
    }
}
