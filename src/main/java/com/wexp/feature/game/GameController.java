package com.wexp.feature.game;

import com.wexp.feature.game.dto.GameIdResponse;
import com.wexp.feature.game.dto.GameResponseDto;
import com.wexp.feature.game.steam.SteamApiService;
import com.wexp.feature.game.steam.dto.SteamGameResponseDto;
import com.wexp.shared.dto.PageResponse;
import com.wexp.shared.exception.ApiException;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.wexp.shared.pagination.AllowedSort;

@RestController
@RequestMapping("/api/v1/games")
@Tag(name = "Game", description = "Pesquisa e detalhes de jogos")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Requisição inválida",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse")))
})
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;
    private final SteamApiService steamApiService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GameResponseDto getGame(@PathVariable String id) throws ApiException {
        return new GameResponseDto(gameService.viewGameDetails(id));
    }

    @GetMapping("/steam/{steamAppId}")
    @ResponseStatus(HttpStatus.OK)
    public GameResponseDto getSteamGameDetails(@PathVariable Long steamAppId) throws ApiException {
        return new GameResponseDto(gameService.viewSteamGameDetails(steamAppId));
    }

    @GetMapping("/steam/{steamAppId}/id")
    @ResponseStatus(HttpStatus.OK)
    public GameIdResponse getGameId(@PathVariable Long steamAppId) {
        return gameService.getGameId(steamAppId);
    }

    @GetMapping("/steam-games")
    @ResponseStatus(HttpStatus.OK)
    @AllowedSort({"name", "acronym", "appid"})
    public PageResponse<SteamGameResponseDto> instantSearch(
            @RequestParam("q") String query,
            @PageableDefault(size = 5) @ParameterObject Pageable pageable) {
        return PageResponse.from(steamApiService.instantSearchGame(query, pageable).map(SteamGameResponseDto::new));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @AllowedSort({"name", "releaseDate", "viewsCount", "steamAppId"})
    public PageResponse<GameResponseDto> search(
            @RequestParam("q") String query,
            @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
        return PageResponse.from(gameService.searchGames(query, pageable).map(GameResponseDto::new));
    }

    @GetMapping("/featured")
    @ResponseStatus(HttpStatus.OK)
    @AllowedSort({"name", "releaseDate", "viewsCount", "steamAppId"})
    public PageResponse<GameResponseDto> featured(
            @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
        var gamesPage = gameService.getMostViewedGames(pageable);
        return PageResponse.from(gamesPage.map(GameResponseDto::new));
    }

    @GetMapping("/released")
    @ResponseStatus(HttpStatus.OK)
    @AllowedSort({"name", "releaseDate", "viewsCount", "steamAppId"})
    public PageResponse<GameResponseDto> recent(
            @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
        return PageResponse.from(gameService.getMostRecentGames(pageable).map(GameResponseDto::new));
    }

}
