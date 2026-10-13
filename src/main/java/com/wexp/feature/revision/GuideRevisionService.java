package com.wexp.feature.revision;

import com.wexp.feature.achievement.IAchievementRepository;
import com.wexp.feature.guide.GuideEntity;
import com.wexp.feature.guide.IGuideRepository;
import com.wexp.feature.revision.dto.CreateGuideRevisionRequest;
import com.wexp.feature.revision.dto.GuideRevisionResponse;
import com.wexp.feature.user.UserEntity;
import com.wexp.shared.exception.ApiException;
import com.wexp.shared.exception.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuideRevisionService {
    private final IGuideRepository guides;
    private final IGuideRevisionRepository revisions;
    private final IGuideRevisionVoteRepository votes;
    private final IAchievementRepository achievements;
    private final GuideRevisionLifecycleService lifecycle;

    @Transactional
    public GuideRevisionResponse createRevision(String guidePublicId, CreateGuideRevisionRequest request, UserEntity user) {
        GuideEntity guide = guides.findByPublicIdForUpdate(guidePublicId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.GuideNotFound));
        requireUser(user);
        lifecycle.initializeVersions(guide);
        GuideRevisionEntity base = revisions.findByPublicId(request.getBaseRevisionId())
                .orElseThrow(() -> new ApiException(ExceptionResponse.RevisionNotFound));
        if (!base.getGuide().getId().equals(guide.getId()) || base.getStatus() != RevisionStatus.PUBLISHED) {
            throw new ApiException(ExceptionResponse.InvalidRevisionBase);
        }
        // The explicit base is preserved even if it has lost the lead while the user was editing.
        GuideRevisionEntity revision = revisions.saveAndFlush(GuideRevisionEntity.builder()
                .guide(guide).author(user).baseRevision(base).content(request.getContent())
                .changeSummary(request.getChangeSummary()).status(RevisionStatus.PUBLISHED).build());
        return new GuideRevisionResponse(revision, 0, false);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Page<GuideRevisionResponse> getRevisions(String guidePublicId, Pageable pageable, UserEntity user) {
        GuideEntity guide = guides.findByPublicId(guidePublicId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.GuideNotFound));
        return page(guide, pageable, user);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Page<GuideRevisionResponse> getRevisionsByAchievement(String achievementId, Pageable pageable, UserEntity user) {
        if (!achievements.existsByPublicId(achievementId)) throw new ApiException(ExceptionResponse.AchievementNotFound);
        GuideEntity guide = guides.findByAchievement_PublicId(achievementId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.GuideNotFound));
        return page(guide, pageable, user);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public GuideRevisionResponse getRevision(String revisionId, UserEntity user) {
        GuideRevisionEntity revision = revisions.findByPublicId(revisionId)
                .filter(r -> r.getStatus() == RevisionStatus.PUBLISHED)
                .orElseThrow(() -> new ApiException(ExceptionResponse.RevisionNotFound));
        boolean voted = user != null && votes.existsByRevision_IdAndUser_Id(revision.getId(), user.getId());
        return new GuideRevisionResponse(revision, votes.countByRevision_Id(revision.getId()), voted);
    }

    private Page<GuideRevisionResponse> page(GuideEntity guide, Pageable pageable, UserEntity user) {
        Page<GuideRevisionEntity> page = revisions.findPublishedByVotes(guide.getId(), RevisionStatus.PUBLISHED, pageable);
        if (page.isEmpty()) return page.map(GuideRevisionResponse::new);
        var ids = page.getContent().stream().map(GuideRevisionEntity::getId).toList();
        Map<Long, Long> counts = votes.countVotesForRevisions(ids).stream()
                .collect(Collectors.toMap(IGuideRevisionVoteRepository.RevisionScore::getRevisionId,
                        IGuideRevisionVoteRepository.RevisionScore::getVoteCount));
        Set<Long> voted = user == null ? Set.of() : Set.copyOf(votes.findVotedRevisionIds(user.getId(), ids));
        return page.map(revision -> new GuideRevisionResponse(revision, counts.getOrDefault(revision.getId(), 0L), voted.contains(revision.getId())));
    }

    static void requireUser(UserEntity user) {
        if (user == null || user.getId() == null) throw new ApiException(ExceptionResponse.Unauthorized);
    }
}
