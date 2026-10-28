package com.wexp.feature.achievement.dto;

import com.wexp.feature.achievement.AchievementEntity;

public record AchievementSummaryDto(
        String id,
        String name,
        Boolean hidden,
        String icon,
        Double globalUnlockPercentage
) {
    public AchievementSummaryDto(AchievementEntity achievement) {
        this(
                achievement.getPublicId(),
                achievement.getName(),
                achievement.getIsHidden(),
                achievement.getIconUrl(),
                achievement.getGlobalUnlockPercentage()
        );
    }
}
