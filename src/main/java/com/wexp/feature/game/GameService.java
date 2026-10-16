package com.wexp.feature.game;

import com.wexp.shared.client.steam.store.SteamStoreClient;
import com.wexp.feature.game.steam.SteamApiService;
import com.wexp.feature.game.steam.SteamGameEntity;
import com.wexp.feature.game.steam.ISteamGameRepository;
import com.wexp.feature.game.steam.GameSynchronizationService;
import com.wexp.feature.game.steam.GameSteamPersistenceService;
import com.wexp.shared.client.steam.store.SteamGameDetails;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    private final IGameRepository gameRepository;
    private final ISteamGameRepository steamGameRepository;

    private final SteamStoreClient steamStoreClient;
    private final SteamApiService steamApiService;
    private final PersistenceTransactions persistenceTransactions;
    private final GameSynchronizationService synchronizationService;
    private final GameSteamPersistenceService steamPersistenceService;
    private final Map<Long, Object> gameCreationLocks = new ConcurrentHashMap<>();


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
        synchronized (gameCreationLocks.computeIfAbsent(steamId, ignored -> new Object())) {
            return getGameIdLocked(steamId);
        }
    }

    private GameIdResponse getGameIdLocked(Long steamId) {
        Optional<GameEntity> existingGame = gameRepository.findBySteamAppId(steamId);
        if (existingGame.isPresent()) {
            synchronizationService.refreshIfStale(existingGame.get());
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

        synchronizationService.refreshIfStale(persistedShell);

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
        Page<GameEntity> localGamesPage = gameRepository.searchByNameOrDescription(query, pageable);
        if (localGamesPage.hasContent()) {
            localGamesPage.forEach(synchronizationService::refreshIfStale);
            return localGamesPage;
        }
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

        return Page.empty(pageable);
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

        synchronizationService.refreshIfStale(game);
        return game;
    }

    private GameEntity getSteamGameDetails(Long steamAppId) throws ApiException {
        synchronized (gameCreationLocks.computeIfAbsent(steamAppId, ignored -> new Object())) {
            return getSteamGameDetailsLocked(steamAppId);
        }
    }

    private GameEntity getSteamGameDetailsLocked(Long steamAppId) throws ApiException {
        Optional<GameEntity> optionalGame = gameRepository.findBySteamAppId(steamAppId);
        if (optionalGame.isPresent()) {
            synchronizationService.refreshIfStale(optionalGame.get());
            return optionalGame.get();
        }

        String newPublicId = PublicIdGenerator.generate(PublicIdType.GAME);

        SteamGameDetails details = steamStoreClient.fetchGameDetails(steamAppId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.SteamGameNotFound));

        try {
            return steamPersistenceService.create(newPublicId, details);
        } catch (DataIntegrityViolationException exception) {
            return gameRepository.findBySteamAppId(steamAppId).orElseThrow(() -> exception);
        }
    }

}
