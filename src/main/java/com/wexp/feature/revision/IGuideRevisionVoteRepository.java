package com.wexp.feature.revision;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IGuideRevisionVoteRepository extends JpaRepository<GuideRevisionVoteEntity, Long> {
    boolean existsByRevision_IdAndUser_Id(Long revisionId, Long userId);
    Optional<GuideRevisionVoteEntity> findByRevision_IdAndUser_Id(Long revisionId, Long userId);
    long countByRevision_Id(Long revisionId);

    interface RevisionScore {
        Long getRevisionId();
        Long getVoteCount();
    }

    @Query("""
            select r.id as revisionId, count(v.id) as voteCount
            from GuideRevisionEntity r left join GuideRevisionVoteEntity v on v.revision = r
            where r.guide.id = :guideId and r.status = :status
            group by r.id, r.createdAt
            order by count(v.id) desc, r.createdAt asc, r.id asc
            """)
    List<RevisionScore> rankPublishedRevisions(@Param("guideId") Long guideId, @Param("status") RevisionStatus status);

    @Query("select v.revision.id from GuideRevisionVoteEntity v where v.user.id = :userId and v.revision.id in :revisionIds")
    List<Long> findVotedRevisionIds(@Param("userId") Long userId, @Param("revisionIds") Collection<Long> revisionIds);

    @Query("""
            select v.revision.id as revisionId, count(v.id) as voteCount
            from GuideRevisionVoteEntity v where v.revision.id in :revisionIds group by v.revision.id
            """)
    List<RevisionScore> countVotesForRevisions(@Param("revisionIds") Collection<Long> revisionIds);
}
