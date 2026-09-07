package com.wexp.service;

import com.wexp.database.model.GuideEntity;
import com.wexp.database.model.GuideRevisionEntity;
import com.wexp.database.model.UserEntity;
import com.wexp.database.repository.IGuideRepository;
import com.wexp.database.repository.IGuideRevisionRepository;
import com.wexp.dto.CreateGuideRevisionRequest;
import com.wexp.dto.GuideRevisionResponse;
import com.wexp.exception.ApiException;
import com.wexp.exception.ExceptionResponse;
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
