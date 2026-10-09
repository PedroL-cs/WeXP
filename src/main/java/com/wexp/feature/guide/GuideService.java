package com.wexp.feature.guide;

import com.wexp.feature.achievement.AchievementEntity;
import com.wexp.feature.user.UserEntity;
import com.wexp.feature.achievement.IAchievementRepository;
import com.wexp.feature.guide.dto.CreateGuideRequestDto;
import com.wexp.shared.exception.ApiException;
import com.wexp.shared.exception.ExceptionResponse;
import com.wexp.shared.persistence.PersistenceTransactions;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuideService {

    private final IGuideRepository guideRepository;
    private final IAchievementRepository achievementRepository;
    private final PersistenceTransactions persistenceTransactions;

    @Transactional(readOnly = true)
    public GuideEntity getGuideByAchievementPublicId(String achievementPublicId) {
        return guideRepository.findByAchievement_PublicId(achievementPublicId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.GuideNotFound));
    }

    @Transactional(readOnly = true)
    public GuideEntity getGuideByPublicId(String publicId) {
        return guideRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.GuideNotFound));
    }

    public GuideEntity createInitialGuide(String achievementPublicId, CreateGuideRequestDto dto, UserEntity currentUser) {
        try {
            return persistenceTransactions.insert(() -> persistInitialGuide(achievementPublicId, dto, currentUser));
        } catch (DataIntegrityViolationException exception) {
            if (guideRepository.existsByAchievement_PublicId(achievementPublicId)) {
                throw new ApiException(ExceptionResponse.GuideAlreadyExists);
            }
            throw exception;
        }
    }

    private GuideEntity persistInitialGuide(String achievementPublicId, CreateGuideRequestDto dto, UserEntity currentUser) {
        if (guideRepository.existsByAchievement_PublicId(achievementPublicId)) {
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

        return guideRepository.saveAndFlush(guideEntity);
    }
}
