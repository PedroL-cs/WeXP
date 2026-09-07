package com.wexp.feature.guide;

import com.wexp.feature.achievement.AchievementEntity;
import com.wexp.feature.user.UserEntity;
import com.wexp.feature.achievement.IAchievementRepository;
import com.wexp.feature.guide.dto.CreateGuideRequestDto;
import com.wexp.shared.exception.ApiException;
import com.wexp.shared.exception.ExceptionResponse;
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
    public GuideEntity createInitialGuide(String achievementPublicId, CreateGuideRequestDto dto, UserEntity currentUser) {
        if (guideRepository.existsByAchievementPublicId(achievementPublicId)) {
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
