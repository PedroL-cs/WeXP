package com.wexp.feature.game.dto;

import com.wexp.feature.achievement.dto.AchievementSummaryDto;
import com.wexp.feature.category.dto.CategoryResponseDto;
import com.wexp.feature.game.GameEntity;
import com.wexp.feature.genre.dto.GenreResponseDto;
import com.wexp.feature.game.GameImageType;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
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
                imageUrls(game.getPublicId())
        );
    }

    private static Map<String, String> imageUrls(String publicId) {
        String baseUrl;
        try {
            baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        } catch (IllegalStateException exception) {
            baseUrl = "http://localhost:8080";
        }
        final String imageBaseUrl = baseUrl;
        return java.util.Arrays.stream(GameImageType.values())
                .collect(java.util.stream.Collectors.toMap(
                        type -> type.name().toLowerCase(),
                        type -> imageBaseUrl + "/api/v1/images/game/" + publicId + "/" + type.name().toLowerCase(),
                        (first, ignored) -> first,
                        java.util.LinkedHashMap::new
                ));
    }
}
