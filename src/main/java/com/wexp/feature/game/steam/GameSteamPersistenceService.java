package com.wexp.feature.game.steam;

import com.wexp.feature.game.GameEntity;
import com.wexp.feature.game.IGameRepository;
import com.wexp.shared.client.steam.store.SteamGameDetails;
import com.wexp.shared.client.steam.store.SteamStoreClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GameSteamPersistenceService {
    private static final Logger logger = LoggerFactory.getLogger(GameSteamPersistenceService.class);
    private final IGameRepository gameRepository;
    private final SteamStoreClient steamStoreClient;

    @Transactional
    public GameEntity create(String publicId, SteamGameDetails details) {
        return persist(publicId, details, null);
    }

    @Transactional
    public void refresh(String publicId, SteamGameDetails details) {
        gameRepository.findByPublicId(publicId).ifPresent(game -> persist(publicId, details, game));
    }

    private GameEntity persist(String publicId, SteamGameDetails details, GameEntity existing) {
        GameEntity game = steamStoreClient.applyDetails(details, publicId, existing);
        game.setSteamSyncedAt(LocalDateTime.now());
        GameEntity saved = gameRepository.saveAndFlush(game);
        // The response has already been fetched; this transaction contains database work only.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    steamStoreClient.scheduleImages(details, saved);
                } catch (RuntimeException exception) {
                    logger.warn("Não foi possível agendar imagens do jogo {}", publicId);
                }
            }
        });
        return saved;
    }
}
