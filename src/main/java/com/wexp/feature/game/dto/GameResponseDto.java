package com.wexp.feature.game.dto;

import com.wexp.feature.achievement.dto.AchievementSummaryDto;
import com.wexp.feature.category.dto.CategoryResponseDto;
import com.wexp.feature.game.GameEntity;
import com.wexp.feature.genre.dto.GenreResponseDto;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record GameResponseDto(
        String id,
        Long steamAppId,
        String name,
        String shortDescription,
        String detailedDescription,
        Long viewsCount,
        LocalDateTime releaseDate,
        List<CategoryResponseDto> categories,
        List<GenreResponseDto> genres,
        List<AchievementSummaryDto> achievements,
        Map<String, String> images
) {
    public GameResponseDto(GameEntity game) {
        this(
                game.getPublicId(),
                game.getSteamAppId(),
                game.getName(),
                game.getShortDescription(),
                game.getDetailedDescription(),
                game.getViewsCount(),
                game.getReleaseDate(),
                game.getCategories() == null
                        ? List.of()
                        : game.getCategories().stream().map(CategoryResponseDto::new).toList(),
                game.getGenres() == null
                        ? List.of()
                        : game.getGenres().stream().map(GenreResponseDto::new).toList(),
                game.getAchievements() == null
                        ? List.of()
                        : game.getAchievements().stream().map(AchievementSummaryDto::new).toList(),
                imageUrls(game)
        );
    }

    private static Map<String, String> imageUrls(GameEntity game) {
        Map<String, String> images = new LinkedHashMap<>();
        images.put("cover", game.getCoverImageUrl());
        images.put("capsule", game.getCapsuleImageUrl());
        images.put("background", game.getBackgroundImageUrl());
        images.put("header", game.getHeaderImageUrl());
        images.put("logo", game.getLogoImageUrl());
        images.put("hero", game.getHeroImageUrl());
        return images;
    }
}
