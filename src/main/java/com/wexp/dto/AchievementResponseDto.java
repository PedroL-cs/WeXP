package com.wexp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wexp.database.model.AchievementEntity;
import com.wexp.database.model.GameEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AchievementResponseDto {
    private final String id;
    private final String name;
    private final String description;
    private final String icon;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "achievements", "achievements_count"})
    private final GameEntity game;

    public AchievementResponseDto(AchievementEntity achievementEntity) {
        this.id = achievementEntity.getPublicId();
        this.name = achievementEntity.getName();
        this.description = achievementEntity.getDescription();
        this.icon = achievementEntity.getIconUrl();
        this.createdAt = achievementEntity.getCreatedAt();
        this.updatedAt = achievementEntity.getUpdatedAt();
        this.game = achievementEntity.getGame();
    }
}
