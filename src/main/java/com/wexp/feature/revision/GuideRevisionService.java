package com.wexp.feature.revision;

import com.wexp.feature.guide.GuideEntity;
import com.wexp.feature.user.UserEntity;
import com.wexp.feature.guide.IGuideRepository;
import com.wexp.feature.revision.dto.CreateGuideRevisionRequest;
import com.wexp.feature.revision.dto.GuideRevisionResponse;
import com.wexp.shared.exception.ApiException;
import com.wexp.shared.exception.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuideRevisionService {

    private final IGuideRepository guideRepository;
    private final IGuideRevisionRepository  revisionRepository;

    @Transactional
    public GuideRevisionResponse createRevision(String guidePublicId, CreateGuideRevisionRequest req, UserEntity currUser) {
        GuideEntity guide = guideRepository.findByPublicId(guidePublicId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.GuideNotFound));

        GuideRevisionEntity revision = GuideRevisionEntity.builder()
                .guide(guide)
                .author(currUser)
                .content(req.getContent())
                .changeSummary(req.getChangeSummary())
                .build();

        revision = revisionRepository.save(revision);
        return new GuideRevisionResponse(revision);
    }
}
