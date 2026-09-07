package com.wexp.service;

import com.wexp.database.model.AchievementEntity;
import com.wexp.database.model.GuideEntity;
import com.wexp.database.model.UserEntity;
import com.wexp.database.repository.IAchievementRepository;
import com.wexp.database.repository.IGuideRepository;
import com.wexp.dto.CreateGuideDTO;
import com.wexp.exception.ApiException;
import com.wexp.exception.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuideService {

    private final IGuideRepository guideRepository;
    private final IAchievementRepository achievementRepository;

    @Transactional(readOnly = true)
    public GuideEntity getGuideByAchievementPublicId(String achievementPublicId) {
        return guideRepository.findByAchievementPublicId(achievementPublicId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.GuideNotFound));
    }

    @Transactional(readOnly = true)
    public GuideEntity getGuideByPublicId(String publicId) {
        return guideRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.GuideNotFound));
    }

    @Transactional
    public GuideEntity createInitialGuide(String achievementPublicId, CreateGuideDTO dto, UserEntity currentUser) {
        if (guideRepository.existingByAchievementPublicId(achievementPublicId)) {
            throw new ApiException(ExceptionResponse.GuideAlreadyExists);
        }

        AchievementEntity achievement = achievementRepository.findByPublicId(achievementPublicId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.AchievementNotFound));

        GuideEntity guideEntity = GuideEntity.builder()
                .content(dto.getContent())
                .achievement(achievement)
                .author(currentUser)
                .version(1)
                .build();

        return guideRepository.save(guideEntity);
    }
}
