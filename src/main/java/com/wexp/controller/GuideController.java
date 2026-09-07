package com.wexp.controller;

import com.wexp.database.model.GuideEntity;
import com.wexp.database.model.UserEntity;
import com.wexp.dto.CreateGuideDTO;
import com.wexp.service.GuideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/guides")
@RequiredArgsConstructor
public class GuideController {

    private final GuideService guideService;

    @GetMapping("/achievements/{achievementPublicId}")
    @ResponseStatus(HttpStatus.OK)
    public GuideEntity getGuideByAchievement(@PathVariable String achievementPublicId) {
        return guideService.getGuideByAchievementPublicId(achievementPublicId);
    }

    @GetMapping("/{publicId}")
    @ResponseStatus(HttpStatus.OK)
    public GuideEntity getGuideByPublicId(@PathVariable String publicId) {
        return guideService.getGuideByPublicId(publicId);
    }

    @PostMapping("/achievements/{achievementPublicId}")
    @ResponseStatus(HttpStatus.CREATED)
    public GuideEntity createInitialGuide(
            @PathVariable String achievementPublicId,
            @Valid @RequestBody CreateGuideDTO dto,
            @AuthenticationPrincipal UserEntity currentUser) {
        return guideService.createInitialGuide(achievementPublicId, dto, currentUser);
    }
}
