package com.wexp.feature.revision.dto;

import com.wexp.feature.revision.GuideRevisionEntity;
import com.wexp.feature.revision.RevisionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class GuideRevisionResponse {
    private String publicId;
    private String guidePublicId;
    private String authorPublicId;
    private String content;
    private RevisionStatus status;
    private LocalDateTime createdAt;

    public GuideRevisionResponse(GuideRevisionEntity revision) {
        this.publicId = revision.getPublicId();
        this.guidePublicId = revision.getGuide().getPublicId();
        this.authorPublicId = revision.getAuthor().getPublicId();
        this.content = revision.getContent();
        this.status = revision.getStatus();
        this.createdAt = revision.getCreatedAt();
    }
}
