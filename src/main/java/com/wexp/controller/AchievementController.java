package com.wexp.controller;

import com.wexp.dto.AchievementResponseDto;
import com.wexp.service.AchievementService;
import lombok.RequiredArgsConstructor;
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
}
