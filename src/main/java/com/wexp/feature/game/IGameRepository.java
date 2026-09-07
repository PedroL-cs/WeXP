package com.wexp.feature.game;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface IGameRepository extends JpaRepository<GameEntity, Long> {
    Optional<GameEntity> findByPublicId(String publicId);
    Optional<GameEntity> findBySteamAppId(Long steamAppId);
    Page<GameEntity> findAllByOrderByViewsCountDesc(Pageable pageable);
    Page<GameEntity> findAllByReleaseDateNotNullOrderByReleaseDateDesc(Pageable pageable);
    Page<GameEntity> findByCategoriesPublicId(String categoryPublicId, Pageable pageable);
    Page<GameEntity> findByGenresPublicId(String genrePublicId, Pageable pageable);

    @Query("""
        SELECT g FROM GameEntity g
            WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :term, '%'))\s
            OR LOWER(g.shortDescription)  LIKE LOWER(CONCAT('%', :term, '%') )\s
   \s""")
    Page<GameEntity> searchByNameOrDescription(@Param("term") String term, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE GameEntity g SET g.viewsCount = g.viewsCount + 1 WHERE g.id = :id")
    void incrementViewsCount(@Param("id") Long id);
}