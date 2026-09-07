package com.wexp.feature.achievement.dto;

import com.wexp.feature.achievement.AchievementEntity;
import com.wexp.feature.game.dto.GameSummaryDto;
import com.wexp.feature.image.ImageService;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AchievementResponseDto {
    private final String id;
    private final String name;
    private final String description;
    private final Boolean isHidden;
    private final String icon;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private final GameSummaryDto game;

    public AchievementResponseDto(AchievementEntity achievementEntity) {
        this.id = achievementEntity.getPublicId();
        this.name = achievementEntity.getName();
        this.description = achievementEntity.getDescription();
        this.isHidden = achievementEntity.getIsHidden();
        this.icon = ImageService.url("achievement", achievementEntity.getPublicId(), "icon");
        this.createdAt = achievementEntity.getCreatedAt();
        this.updatedAt = achievementEntity.getUpdatedAt();
        this.game = new GameSummaryDto(achievementEntity.getGame());
    }

    public AchievementResponseDto(AchievementEntity achievementEntity, boolean includeGame) {
        this.id = achievementEntity.getPublicId();
        this.name = achievementEntity.getName();
        this.description = achievementEntity.getDescription();
        this.isHidden = achievementEntity.getIsHidden();
        this.icon = ImageService.url("achievement", achievementEntity.getPublicId(), "icon");
        this.createdAt = achievementEntity.getCreatedAt();
        this.updatedAt = achievementEntity.getUpdatedAt();
        this.game = includeGame ? new GameSummaryDto(achievementEntity.getGame()) : null;
    }
}
