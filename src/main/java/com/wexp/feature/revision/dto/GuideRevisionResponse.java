package com.wexp.feature.revision.dto;

import com.wexp.feature.revision.GuideRevisionEntity;
import com.wexp.feature.revision.RevisionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class GuideRevisionResponse {
    private String publicId;
    private String guidePublicId;
    private String authorPublicId;
    private String content;
    private String changeSummary;
    private RevisionStatus status;
    private LocalDateTime createdAt;
    private String author;
    private String baseRevisionId;
    private long voteCount;
    private boolean viewerHasVoted;
    @Getter(onMethod_ = @JsonProperty("isMain"))
    private boolean isMain;
    @Getter(onMethod_ = @JsonProperty("isOriginal"))
    private boolean isOriginal;
    @Getter(onMethod_ = @JsonProperty("isBasedOnCurrentMain"))
    private boolean isBasedOnCurrentMain;

    public GuideRevisionResponse(GuideRevisionEntity revision) {
        this.publicId = revision.getPublicId();
        this.guidePublicId = revision.getGuide().getPublicId();
        this.authorPublicId = revision.getAuthor().getPublicId();
        this.content = revision.getContent();
        this.changeSummary = revision.getChangeSummary();
        this.status = revision.getStatus();
        this.createdAt = revision.getCreatedAt();
        this.author = revision.getAuthor().getUsername();
        this.baseRevisionId = revision.getBaseRevision() == null ? null : revision.getBaseRevision().getPublicId();
    }

    public GuideRevisionResponse(GuideRevisionEntity revision, long voteCount, boolean viewerHasVoted) {
        this(revision);
        this.voteCount = voteCount;
        this.viewerHasVoted = viewerHasVoted;
        var guide = revision.getGuide();
        this.isMain = guide.getActiveRevision() != null && revision.getId().equals(guide.getActiveRevision().getId());
        this.isOriginal = guide.getOriginalRevision() != null && revision.getId().equals(guide.getOriginalRevision().getId());
        this.isBasedOnCurrentMain = revision.getBaseRevision() != null && guide.getActiveRevision() != null
                && revision.getBaseRevision().getId().equals(guide.getActiveRevision().getId());
    }
}
