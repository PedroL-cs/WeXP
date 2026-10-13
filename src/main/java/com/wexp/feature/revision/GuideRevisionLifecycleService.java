package com.wexp.feature.revision;

import com.wexp.feature.guide.GuideEntity;
import com.wexp.feature.guide.IGuideRepository;
import com.wexp.shared.exception.ApiException;
import com.wexp.shared.exception.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuideRevisionLifecycleService {
    public static final List<RevisionStatus> LEGACY_STATUSES = List.of(RevisionStatus.PENDING, RevisionStatus.APPROVED);
    private final IGuideRepository guides;
    private final IGuideRevisionRepository revisions;

    @Transactional
    public void initializeVersions(GuideEntity guide) {
        if (guide.getOriginalRevision() == null) {
            GuideRevisionEntity original = revisions.saveAndFlush(GuideRevisionEntity.builder()
                    .guide(guide).author(guide.getAuthor()).content(guide.getContent())
                    .changeSummary("Versão original do guia").createdAt(guide.getCreatedAt())
                    .status(RevisionStatus.PUBLISHED).build());
            guide.setOriginalRevision(original);
        }
        if (guide.getActiveRevision() == null) guide.setActiveRevision(guide.getOriginalRevision());
        if (!guide.getId().equals(guide.getOriginalRevision().getGuide().getId())
                || !guide.getId().equals(guide.getActiveRevision().getGuide().getId())) {
            throw new IllegalStateException("As versões original e principal devem pertencer ao próprio guia");
        }
        guides.saveAndFlush(guide);
    }

    @Transactional
    public void migrateGuide(String publicId) {
        GuideEntity guide = guides.findByPublicIdForUpdate(publicId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.GuideNotFound));
        initializeVersions(guide);
        revisions.publishLegacyRevisions(guide.getId(), RevisionStatus.PUBLISHED, LEGACY_STATUSES);
        revisions.backfillLegacyBases(guide.getId(), guide.getOriginalRevision(), guide.getOriginalRevision().getId());
    }
}
