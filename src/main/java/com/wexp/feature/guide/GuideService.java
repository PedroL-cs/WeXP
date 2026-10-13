package com.wexp.feature.guide;

import com.wexp.feature.achievement.AchievementEntity;
import com.wexp.feature.user.UserEntity;
import com.wexp.feature.achievement.IAchievementRepository;
import com.wexp.feature.guide.dto.CreateGuideRequestDto;
import com.wexp.feature.guide.dto.GuideResponseDto;
import com.wexp.feature.revision.GuideRevisionLifecycleService;
import com.wexp.feature.revision.IGuideRevisionVoteRepository;
import com.wexp.shared.exception.ApiException;
import com.wexp.shared.exception.ExceptionResponse;
import com.wexp.shared.persistence.PersistenceTransactions;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

@Service
@RequiredArgsConstructor
public class GuideService {

    private final IGuideRepository guideRepository;
    private final IAchievementRepository achievementRepository;
    private final PersistenceTransactions persistenceTransactions;
    private final GuideRevisionLifecycleService revisionLifecycle;
    private final IGuideRevisionVoteRepository votes;

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public GuideResponseDto getGuideByAchievementPublicId(String achievementPublicId) {
        return response(guideRepository.findByAchievement_PublicId(achievementPublicId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.GuideNotFound)));
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public GuideResponseDto getGuideByPublicId(String publicId) {
        return response(guideRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.GuideNotFound)));
    }

    public GuideResponseDto createInitialGuide(String achievementPublicId, CreateGuideRequestDto dto, UserEntity currentUser) {
        try {
            return persistenceTransactions.insert(() -> {
                GuideEntity guide = persistInitialGuide(achievementPublicId, dto, currentUser);
                revisionLifecycle.initializeVersions(guide);
                return response(guide);
            });
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

    private GuideResponseDto response(GuideEntity guide) {
        long count = guide.getActiveRevision() == null ? 0 : votes.countByRevision_Id(guide.getActiveRevision().getId());
        return new GuideResponseDto(guide, count);
    }
}
