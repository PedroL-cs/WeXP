package com.wexp.feature.achievement.dto;

import com.wexp.feature.achievement.AchievementEntity;
import com.wexp.feature.image.ImageService;

public record AchievementSummaryDto(
        String id,
        String name,
        Boolean hidden,
        String icon
) {
    public AchievementSummaryDto(AchievementEntity achievement) {
        this(
                achievement.getPublicId(),
                achievement.getName(),
                achievement.getIsHidden(),
                ImageService.url("achievement", achievement.getPublicId(), "icon")
        );
    }
}
