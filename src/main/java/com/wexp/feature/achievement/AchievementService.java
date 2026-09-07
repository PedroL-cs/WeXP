package com.wexp.feature.achievement;

import com.wexp.feature.game.IGameRepository;
import com.wexp.feature.achievement.dto.AchievementResponseDto;
import com.wexp.shared.exception.ApiException;
import com.wexp.shared.exception.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final IGameRepository gameRepository;
    private final IAchievementRepository achievementRepository;

    public AchievementResponseDto getAchievementDetails(String publicId) {
        AchievementEntity achievementEntity = achievementRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.AchievementNotFound));
        return new AchievementResponseDto(achievementEntity);
    }

    public Page<AchievementResponseDto> searchAchievement(String query, Boolean isHidden, Pageable pageable) {
        return achievementRepository.searchAchievement(query, isHidden, pageable).map(AchievementResponseDto::new);
    }

    public Page<AchievementResponseDto> getAchievementList(String gameId, String query, Boolean isHidden, Pageable pageable) {
        gameRepository.findByPublicId(gameId).orElseThrow(() -> new ApiException(ExceptionResponse.GameNotFound));

        return achievementRepository
                .findByGameAndFilters(gameId, query, isHidden, pageable)
                .map(entity -> new  AchievementResponseDto(entity, false));
    }
}
