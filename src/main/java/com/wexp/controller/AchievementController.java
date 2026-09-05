package com.wexp.controller;

import com.wexp.dto.AchievementResponseDto;
import com.wexp.dto.PageResponse;
import com.wexp.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping("/{achievementId}")
    @ResponseStatus(HttpStatus.OK)
    public AchievementResponseDto getAchievementDetails(@PathVariable String achievementId) {
        return achievementService.getAchievementDetails(achievementId);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<AchievementResponseDto> searchAchievements(
            @RequestParam("q") String query,
            @RequestParam(name = "hidden", required = false) Boolean hidden,
            @PageableDefault(size = 20) Pageable pageable) {
        var achivementsPage = achievementService.searchAchievement(query, hidden, pageable);
        return PageResponse.from(achivementsPage);
    }

    @GetMapping("/game/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<AchievementResponseDto> getAchievementListByGameId(
            @PathVariable String gameId,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "hidden", required = false) Boolean hidden,
            @PageableDefault(size = 20) Pageable pageable) {
        var achivementsPage = achievementService.getAchievementList(gameId, query, hidden, pageable);
        return PageResponse.from(achivementsPage);
    }
}
