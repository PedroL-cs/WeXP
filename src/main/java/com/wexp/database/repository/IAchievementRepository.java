package com.wexp.database.repository;

import com.wexp.database.model.AchievementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IAchievementRepository extends JpaRepository<AchievementEntity, Long> {
    Optional<AchievementEntity> findByPublicId(String publicId);
}
