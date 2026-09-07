package com.wexp.dto;

import com.wexp.database.model.GuideRevisionEntity;
import com.wexp.database.model.RevisionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
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
