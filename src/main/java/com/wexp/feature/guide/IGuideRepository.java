package com.wexp.feature.guide;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IGuideRepository extends JpaRepository<GuideEntity, Long> {
    Optional<GuideEntity> findByPublicId(String publicId);
    Optional<GuideEntity> findByAchievementPublicId(String achievementPublicId);
    Boolean existsByAchievementPublicId(String achievementPublicId);
}
