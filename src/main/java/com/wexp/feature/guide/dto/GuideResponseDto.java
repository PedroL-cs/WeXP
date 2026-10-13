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
        LocalDateTime updatedAt,
        String activeRevisionId,
        String originalRevisionId,
        String authorPublicId,
        String creatorPublicId,
        String creator,
        long voteCount
) {
    public GuideResponseDto(GuideEntity guide) {
        this(guide, 0);
    }

    public GuideResponseDto(GuideEntity guide, long voteCount) {
        this(
                guide.getPublicId(),
                guide.getActiveRevision() == null ? guide.getContent() : guide.getActiveRevision().getContent(),
                guide.getVersion(),
                guide.getAchievementPublicId(),
                guide.getActiveRevision() == null ? guide.getAuthorUsername() : guide.getActiveRevision().getAuthor().getUsername(),
                guide.getCreatedAt(),
                guide.getUpdatedAt(),
                guide.getActiveRevision() == null ? null : guide.getActiveRevision().getPublicId(),
                guide.getOriginalRevision() == null ? null : guide.getOriginalRevision().getPublicId(),
                guide.getActiveRevision() == null ? (guide.getAuthor() == null ? null : guide.getAuthor().getPublicId())
                        : guide.getActiveRevision().getAuthor().getPublicId(),
                guide.getAuthor() == null ? null : guide.getAuthor().getPublicId(),
                guide.getAuthorUsername(),
                voteCount
        );
    }
}
