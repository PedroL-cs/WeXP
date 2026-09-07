package com.wexp.database.repository;

import com.wexp.database.model.GuideEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IGuideRepository extends JpaRepository<GuideEntity, Long> {
    Optional<GuideEntity> findByPublicId(String publicId);
    Optional<GuideEntity> findByAchievementPublicId(String achievementPublicId);
    Boolean existingByAchievementPublicId(String achievementPublicId);
}
