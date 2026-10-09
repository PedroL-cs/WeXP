package com.wexp.feature.game;

import com.wexp.shared.client.steam.store.SteamStoreClient;
import com.wexp.feature.game.steam.SteamApiService;
import com.wexp.feature.game.steam.SteamGameEntity;
import com.wexp.feature.game.steam.ISteamGameRepository;
import com.wexp.feature.game.dto.GameIdResponse;
import com.wexp.shared.exception.ApiException;
import com.wexp.shared.exception.ExceptionResponse;
import com.wexp.shared.util.PublicIdGenerator;
import com.wexp.shared.util.PublicIdType;
import com.wexp.shared.persistence.PersistenceTransactions;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private final IGameRepository gameRepository;
    private final ISteamGameRepository steamGameRepository;

    private final SteamStoreClient steamStoreClient;
    private final SteamApiService steamApiService;
    private final PersistenceTransactions persistenceTransactions;


    public GameEntity viewGameDetails(String publicId) throws ApiException {
        GameEntity game = getGameDetails(publicId);

        gameRepository.incrementViewsCount(game.getId());
        game.setViewsCount(game.getViewsCount() + 1);

        return game;
    }

    public GameEntity viewSteamGameDetails(Long steamAppId) throws ApiException {
        GameEntity game = getSteamGameDetails(steamAppId);

        gameRepository.incrementViewsCount(game.getId());
        game.setViewsCount(game.getViewsCount() + 1);

        return game;
    }

    public GameIdResponse getGameId(Long steamId) {
        Optional<GameEntity> existingGame = gameRepository.findBySteamAppId(steamId);
        if (existingGame.isPresent()) {
            return GameIdResponse.builder()
                    .steamId(steamId)
                    .gameId(existingGame.get().getPublicId())
                    .gameStatus(GameIdResponse.GameStatus.EXISTING)
                    .build();
        }

        String newPublicId = PublicIdGenerator.generate(PublicIdType.GAME);
        GameEntity shell = GameEntity.builder()
                .publicId(newPublicId)
                .steamAppId(steamId)
                .name(shellNameFor(steamId))
                .build();

        GameEntity persistedShell;
        try {
            persistedShell = persistenceTransactions.insert(() -> gameRepository.saveAndFlush(shell));
        } catch (DataIntegrityViolationException e) {
            GameEntity winner = gameRepository.findBySteamAppId(steamId).orElseThrow(() -> e);
            logger.warn("Conflito de inserção evitado: O jogo com Steam ID {} já foi criado por outra thread.", steamId);
            return GameIdResponse.builder()
                    .steamId(steamId)
                    .gameId(winner.getPublicId())
                    .gameStatus(GameIdResponse.GameStatus.EXISTING)
                    .build();
        }

        GameEntity gameToEnrich = persistedShell;
        CompletableFuture.runAsync(() -> {
            try {
                steamStoreClient.getGameDetails(steamId, gameToEnrich.getPublicId(), gameToEnrich)
                        .ifPresent(gameRepository::save);
            } catch (DataIntegrityViolationException e) {
                logger.warn("Conflito de inserção evitado: O jogo com Steam ID {} foi inserido por outra thread.", steamId);
            } catch (Exception e) {
                logger.error("Erro ao salvar dados do jogo Steam ID: {}", steamId, e);
            }
        });

        return GameIdResponse.builder()
                .steamId(steamId)
                .gameId(persistedShell.getPublicId())
                .gameStatus(GameIdResponse.GameStatus.NEW)
                .build();
    }

    private String shellNameFor(Long steamAppId) {
        return steamGameRepository.findByAppid(steamAppId)
                .map(SteamGameEntity::getName)
                .orElse("Jogo Steam #" + steamAppId);
    }

    public Page<GameEntity> searchGames(String query, Pageable pageable) {
        Page<SteamGameEntity> instantResult = steamApiService.instantSearchGame(query, pageable);

        if (instantResult.hasContent()) {
            List<GameEntity> games = instantResult.getContent().stream()
                    .map(steamGame -> {
                        try { return getSteamGameDetails(steamGame.getAppid()); }
                        catch (ApiException ignored) { return null; }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            if (!games.isEmpty()) return new PageImpl<>(games, pageable, instantResult.getTotalElements());
        }

        List<SteamGameEntity> storeResults = steamStoreClient.searchGames(query);
        if (storeResults != null && !storeResults.isEmpty()) {
            steamGameRepository.saveAll(storeResults);

            List<GameEntity> games = storeResults.stream()
                    .map(steamGame -> {
                        try { return getSteamGameDetails(steamGame.getAppid()); }
                        catch (ApiException ignored) { return null; }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            if (!games.isEmpty()) return new PageImpl<>(games, pageable, storeResults.size());
        }

        Page<GameEntity> localGamesPage = gameRepository.searchByNameOrDescription(query, pageable);
        if (!localGamesPage.hasContent()) return Page.empty(pageable);

        List<GameEntity> updatedGames = localGamesPage.getContent().stream()
                .map(game -> {
                    try { return getGameDetails(game.getPublicId()); }
                    catch (ApiException ignored) { return game; }
                })
                .toList();

        return new PageImpl<>(updatedGames, pageable, localGamesPage.getTotalElements());
    }

    public Page<GameEntity> getMostViewedGames(Pageable pageable) {
        return gameRepository.findAllByOrderByViewsCountDesc(pageable);
    }

    public Page<GameEntity> getMostRecentGames(Pageable pageable) {
        return gameRepository.findAllByReleaseDateNotNullOrderByReleaseDateDesc(pageable);
    }

    private GameEntity getGameDetails(String publicId) throws ApiException {
        GameEntity game = gameRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.GameNotFound));

        return getUpdatedSteamGame(game);
    }

    private GameEntity getSteamGameDetails(Long steamAppId) throws ApiException {
        Optional<GameEntity> optionalGame = gameRepository.findBySteamAppId(steamAppId);
        if (optionalGame.isPresent()) return getUpdatedSteamGame(optionalGame.get());

        String newPublicId = PublicIdGenerator.generate(PublicIdType.GAME);

        GameEntity game = steamStoreClient.getGameDetails(steamAppId, newPublicId, null)
                .orElseThrow(() -> new ApiException(ExceptionResponse.SteamGameNotFound));

        try {
            return persistenceTransactions.insert(() -> gameRepository.saveAndFlush(game));
        } catch (DataIntegrityViolationException exception) {
            return gameRepository.findBySteamAppId(steamAppId).orElseThrow(() -> exception);
        }
    }

    private GameEntity getUpdatedSteamGame(GameEntity game) throws ApiException {
        LocalDateTime updatedAt = game.getUpdatedAt();
        if (updatedAt != null && updatedAt.isAfter(LocalDateTime.now().minusDays(7))) return game;

        GameEntity updatedGame = steamStoreClient.getGameDetails(game.getSteamAppId(), game.getPublicId(), game)
                .orElseThrow(() -> new ApiException(ExceptionResponse.SteamGameNotFound));

        updatedGame.setUpdatedAt(LocalDateTime.now());
        return gameRepository.save(updatedGame);
    }
}
