package com.wexp.service;

import com.wexp.database.model.AchievementEntity;
import com.wexp.database.repository.IAchievementRepository;
import com.wexp.dto.AchievementResponseDto;
import com.wexp.exception.ApiException;
import com.wexp.exception.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final IAchievementRepository achievementRepository;

    public AchievementResponseDto getAchievementDetails(String publicId) {
        AchievementEntity achievementEntity = achievementRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.AchievementNotFound));
        return  new AchievementResponseDto(achievementEntity);
    }

}
