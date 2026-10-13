package com.wexp.feature.revision.dto;

public record RevisionVoteResponse(
        String revisionId,
        long voteCount,
        boolean viewerHasVoted,
        boolean isMain,
        String activeRevisionId,
        int guideVersion
) {
}
