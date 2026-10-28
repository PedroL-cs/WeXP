package com.wexp.feature.game.steam;

import com.wexp.feature.game.GameEntity;
import com.wexp.feature.game.IGameRepository;
import com.wexp.shared.client.steam.store.SteamGameDetails;
import com.wexp.shared.client.steam.store.SteamStoreClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GameSteamPersistenceService {
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
        game.setAchievementPercentagesCheckedAt(LocalDateTime.now());
        return gameRepository.saveAndFlush(game);
    }
}
