package com.wexp.feature.guide;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IGuideRepository extends JpaRepository<GuideEntity, Long> {
    Optional<GuideEntity> findByPublicId(String publicId);
    Boolean existsByPublicId(String publicId);
    Optional<GuideEntity> findByAchievement_PublicId(String achievementPublicId);
    Boolean existsByAchievement_PublicId(String achievementPublicId);
}
