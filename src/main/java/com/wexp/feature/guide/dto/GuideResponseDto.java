package com.wexp.feature.guide.dto;

import com.wexp.feature.guide.GuideEntity;

import java.time.LocalDateTime;

public record GuideResponseDto(
        String id,
        String content,
        Integer version,
        String achievementId,
        String author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public GuideResponseDto(GuideEntity guide) {
        this(
                guide.getPublicId(),
                guide.getContent(),
                guide.getVersion(),
                guide.getAchievementPublicId(),
                guide.getAuthorUsername(),
                guide.getCreatedAt(),
                guide.getUpdatedAt()
        );
    }
}
