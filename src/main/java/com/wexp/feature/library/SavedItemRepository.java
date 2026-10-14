package com.wexp.feature.library;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SavedItemRepository extends JpaRepository<SavedItemEntity, Long> {

    @EntityGraph(attributePaths = {"game", "achievement", "guide"})
    Page<SavedItemEntity> findByUser_Id(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"game", "achievement", "guide"})
    Page<SavedItemEntity> findByUser_IdAndResourceType(
            Long userId,
            SavedResourceType resourceType,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "game")
    Optional<SavedItemEntity> findByUser_IdAndGame_PublicId(Long userId, String gamePublicId);

    @EntityGraph(attributePaths = "achievement")
    Optional<SavedItemEntity> findByUser_IdAndAchievement_PublicId(Long userId, String achievementPublicId);

    @EntityGraph(attributePaths = "guide")
    Optional<SavedItemEntity> findByUser_IdAndGuide_PublicId(Long userId, String guidePublicId);
}
