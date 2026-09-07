package com.wexp.feature.achievement;

import com.wexp.feature.achievement.dto.AchievementResponseDto;
import com.wexp.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springdoc.core.annotations.ParameterObject;
import com.wexp.shared.pagination.AllowedSort;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Achievement", description = "Pesquisa e detalhes de conquistas")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Requisição inválida",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse")))
})
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping("/achievements/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AchievementResponseDto getAchievementDetails(@PathVariable String id) {
        return achievementService.getAchievementDetails(id);
    }

    @GetMapping("/achievements")
    @ResponseStatus(HttpStatus.OK)
    @AllowedSort({"name", "createdAt", "updatedAt", "isHidden"})
    public PageResponse<AchievementResponseDto> searchAchievements(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "hidden", required = false) Boolean hidden,
            @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
        return PageResponse.from(achievementService.searchAchievement(query, hidden, pageable));
    }

    @GetMapping("/games/{gameId}/achievements")
    @ResponseStatus(HttpStatus.OK)
    @AllowedSort({"name", "createdAt", "updatedAt", "isHidden"})
    public PageResponse<AchievementResponseDto> getAchievementListByGameId(
            @PathVariable String gameId,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "hidden", required = false) Boolean hidden,
            @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
        var achivementsPage = achievementService.getAchievementList(gameId, query, hidden, pageable);
        return PageResponse.from(achivementsPage);
    }
}
