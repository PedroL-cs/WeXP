package com.wexp.controller;

import com.wexp.database.model.UserEntity;
import com.wexp.dto.CreateGuideRevisionRequest;
import com.wexp.dto.GuideRevisionResponse;
import com.wexp.service.GuideRevisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/guides")
@RequiredArgsConstructor
public class GuideRevisionController {
    private final GuideRevisionService revisionService;

    @PostMapping("/{guidePublicId}/revisions")
    @ResponseStatus(HttpStatus.CREATED)
    public GuideRevisionResponse createRevision(
            @PathVariable String guidePublicId,
            @Valid @RequestBody CreateGuideRevisionRequest request,
            @AuthenticationPrincipal UserEntity currentUser) {
        return revisionService.createRevision(guidePublicId, request, currentUser);
    }

}
