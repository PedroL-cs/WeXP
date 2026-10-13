package com.wexp.feature.guide;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import com.wexp.feature.revision.RevisionStatus;

import java.util.List;
import java.util.Optional;

public interface IGuideRepository extends JpaRepository<GuideEntity, Long> {
    Optional<GuideEntity> findByPublicId(String publicId);
    Boolean existsByPublicId(String publicId);
    Optional<GuideEntity> findByAchievement_PublicId(String achievementPublicId);
    Boolean existsByAchievement_PublicId(String achievementPublicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from GuideEntity g where g.publicId = :publicId")
    Optional<GuideEntity> findByPublicIdForUpdate(@Param("publicId") String publicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from GuideEntity g where g.id = :id")
    Optional<GuideEntity> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select g.publicId from GuideEntity g
            where g.originalRevision is null or g.activeRevision is null
               or exists (select r.id from GuideRevisionEntity r where r.guide = g and r.status in :legacyStatuses)
            order by g.id
            """)
    List<String> findGuidesNeedingRevisionMigration(@Param("legacyStatuses") List<RevisionStatus> legacyStatuses, Pageable pageable);
}
