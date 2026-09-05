package com.wexp.database.repository;

import com.wexp.database.model.AchievementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IAchievementRepository extends JpaRepository<AchievementEntity, Long> {
    Optional<AchievementEntity> findByPublicId(String publicId);

    @Query("""
    SELECT a FROM AchievementEntity a
    WHERE (:isHidden IS NULL OR a.isHidden = :isHidden)
        AND (
            :term IS NULL OR :term = '' OR
            LOWER(a.name) LIKE LOWER(CONCAT('%', :term, '%')) OR
            (a.description IS NOT NULL AND LOWER(a.description) LIKE LOWER(CONCAT('%', :term, '%')))
        )
    """)
    Page<AchievementEntity> searchAchievement(
            @Param("term") String term,
            @Param("isHidden") Boolean isHidden,
            Pageable pageable);

    @Query("""
        SELECT a FROM AchievementEntity a
        WHERE a.game.publicId = :gamePublicId
            AND (:isHidden IS NULL OR a.isHidden = :isHidden)
            AND (
                :term IS NULL OR :term = '' OR
                LOWER(a.name) LIKE LOWER(CONCAT('%', :term, '%')) OR
                (a.description IS NOT NULL AND LOWER(a.description) LIKE LOWER(CONCAT('%', :term, '%')))
            )
    """)
    Page<AchievementEntity> findByGameAndFilters(
            @Param("gamePublicId") String gamePublicId,
            @Param("term") String term,
            @Param("isHidden") Boolean isHidden,
            Pageable pageable);
}
