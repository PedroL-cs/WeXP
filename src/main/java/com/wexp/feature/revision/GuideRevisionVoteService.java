package com.wexp.feature.revision;

import com.wexp.feature.guide.GuideEntity;
import com.wexp.feature.guide.IGuideRepository;
import com.wexp.feature.revision.dto.RevisionVoteResponse;
import com.wexp.feature.user.UserEntity;
import com.wexp.shared.exception.ApiException;
import com.wexp.shared.exception.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuideRevisionVoteService {
    private final IGuideRepository guides;
    private final IGuideRevisionRepository revisions;
    private final IGuideRevisionVoteRepository votes;

    @Transactional
    public RevisionVoteResponse upvote(String revisionId, UserEntity user) {
        return changeVote(revisionId, user, true);
    }

    @Transactional
    public RevisionVoteResponse removeUpvote(String revisionId, UserEntity user) {
        return changeVote(revisionId, user, false);
    }

    private RevisionVoteResponse changeVote(String revisionId, UserEntity user, boolean add) {
        GuideRevisionService.requireUser(user);
        Long guideId = revisions.findGuideIdByPublicId(revisionId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.RevisionNotFound));
        // Every vote for this guide takes the same lock before changing votes or selecting a winner.
        GuideEntity guide = guides.findByIdForUpdate(guideId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.GuideNotFound));
        GuideRevisionEntity revision = revisions.findByPublicId(revisionId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.RevisionNotFound));
        if (revision.getStatus() != RevisionStatus.PUBLISHED) throw new ApiException(ExceptionResponse.RevisionNotEligible);
        if (add && revision.getAuthor().getId().equals(user.getId())) throw new ApiException(ExceptionResponse.OwnRevisionVote);

        var existing = votes.findByRevision_IdAndUser_Id(revision.getId(), user.getId());
        if (add && existing.isEmpty()) {
            votes.saveAndFlush(GuideRevisionVoteEntity.builder().revision(revision).user(user).build());
        } else if (!add && existing.isPresent()) {
            votes.delete(existing.get());
            votes.flush();
        }
        selectMain(guide);
        return new RevisionVoteResponse(revision.getPublicId(), votes.countByRevision_Id(revision.getId()), add,
                guide.getActiveRevision().getId().equals(revision.getId()), guide.getActiveRevision().getPublicId(), guide.getVersion());
    }

    private void selectMain(GuideEntity guide) {
        var ranking = votes.rankPublishedRevisions(guide.getId(), RevisionStatus.PUBLISHED);
        if (ranking.isEmpty()) throw new ApiException(ExceptionResponse.RevisionNotEligible);
        var top = ranking.get(0);
        if (guide.getActiveRevision() != null) {
            var current = ranking.stream().filter(score -> score.getRevisionId().equals(guide.getActiveRevision().getId())).findFirst();
            // The current main keeps the lead on a tie, including when every score is zero.
            if (current.isPresent() && current.get().getVoteCount().equals(top.getVoteCount())) return;
        }
        GuideRevisionEntity winner = revisions.findById(top.getRevisionId()).orElseThrow();
        guide.setActiveRevision(winner);
        guide.setVersion(guide.getVersion() + 1);
        guides.saveAndFlush(guide);
    }
}
