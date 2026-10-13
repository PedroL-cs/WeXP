package com.wexp.feature.revision;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IGuideRevisionRepository extends JpaRepository<GuideRevisionEntity, Long> {
    Optional<GuideRevisionEntity> findByPublicId(String publicId);
    @Query("select r.guide.id from GuideRevisionEntity r where r.publicId = :publicId")
    Optional<Long> findGuideIdByPublicId(@Param("publicId") String publicId);
    List<GuideRevisionEntity> findAllByGuide_PublicIdOrderByCreatedAtDesc(String guidePublicId);

    @Query(value = """
            select r from GuideRevisionEntity r where r.guide.id = :guideId and r.status = :status
            order by (select count(v.id) from GuideRevisionVoteEntity v where v.revision = r) desc,
                     r.createdAt asc, r.id asc
            """, countQuery = "select count(r) from GuideRevisionEntity r where r.guide.id = :guideId and r.status = :status")
    Page<GuideRevisionEntity> findPublishedByVotes(@Param("guideId") Long guideId, @Param("status") RevisionStatus status, Pageable pageable);

    @Modifying(flushAutomatically = true)
    @Query("update GuideRevisionEntity r set r.status = :published where r.guide.id = :guideId and r.status in :legacyStatuses")
    int publishLegacyRevisions(@Param("guideId") Long guideId, @Param("published") RevisionStatus published,
                              @Param("legacyStatuses") List<RevisionStatus> legacyStatuses);

    @Modifying(flushAutomatically = true)
    @Query("update GuideRevisionEntity r set r.baseRevision = :original where r.guide.id = :guideId and r.baseRevision is null and r.id <> :originalId")
    int backfillLegacyBases(@Param("guideId") Long guideId, @Param("original") GuideRevisionEntity original,
                            @Param("originalId") Long originalId);
}
