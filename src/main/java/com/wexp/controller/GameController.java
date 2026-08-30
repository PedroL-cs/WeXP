package com.wexp.controller;

import com.wexp.database.model.GameEntity;
import com.wexp.database.model.SteamGameEntity;
import com.wexp.dto.GameIdResponse;
import com.wexp.dto.PageResponse;
import com.wexp.exception.ApiException;
import com.wexp.service.GameService;
import com.wexp.service.SteamApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;
    private final SteamApiService steamApiService;

    @GetMapping("/{publicId}")
    @ResponseStatus(HttpStatus.OK)
    public GameEntity getGame(@PathVariable String publicId) throws ApiException {
        return gameService.viewGameDetails(publicId);
    }

    @GetMapping("/steam/{steamAppId}")
    @ResponseStatus(HttpStatus.OK)
    public GameEntity getSteamGameDetails(@PathVariable Long steamAppId) throws ApiException {
        return gameService.viewSteamGameDetails(steamAppId);
    }

    @GetMapping("/steam/{steamAppId}/id")
    @ResponseStatus(HttpStatus.OK)
    public GameIdResponse getGameId(@PathVariable Long steamAppId) {
        return gameService.getGameId(steamAppId);
    }

    @GetMapping("/search/instant")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<SteamGameEntity> instantSearch(
            @RequestParam("q") String query,
            @PageableDefault(size = 5) Pageable pageable) {
        Page<SteamGameEntity> steamGamesPage= steamApiService.instantSearchGame(query, pageable);
        return PageResponse.from(steamGamesPage);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<GameEntity> search(
            @RequestParam("q") String query,
            @PageableDefault(size = 20) Pageable pageable) {
         Page<GameEntity> gamesPage = gameService.searchGames(query, pageable);
        return PageResponse.from(gamesPage);
    }

    @GetMapping("/featured")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<GameEntity> featured(
            @PageableDefault(size = 20) Pageable pageable) {
        var gamesPage = gameService.getMostViewedGames(pageable);
        return PageResponse.from(gamesPage);
    }

    @GetMapping("/released")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<GameEntity> recent(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GameEntity> gamesPage = gameService.getMostRecentGames(pageable);
        return PageResponse.from(gamesPage);
    }

}
