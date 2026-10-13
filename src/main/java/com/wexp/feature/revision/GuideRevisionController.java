package com.wexp.feature.revision;

import com.wexp.feature.user.UserEntity;
import com.wexp.feature.revision.dto.CreateGuideRevisionRequest;
import com.wexp.feature.revision.dto.GuideRevisionResponse;
import com.wexp.feature.revision.dto.RevisionVoteResponse;
import com.wexp.shared.dto.PageResponse;
import com.wexp.shared.pagination.AllowedSort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1")
@Tag(name = "Guide Revision", description = "Revisões de guias")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Requisição inválida",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "401", description = "Autenticação obrigatória",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "403", description = "Não é permitido votar na própria versão",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "409", description = "Revisão não disponível para votação",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse")))
})
@RequiredArgsConstructor
public class GuideRevisionController {
    private final GuideRevisionService revisionService;
    private final GuideRevisionVoteService voteService;

    @GetMapping("/guides/{guideId}/revisions")
    @AllowedSort({})
    public PageResponse<GuideRevisionResponse> getRevisions(
            @PathVariable String guideId,
            @PageableDefault(size = 20) @ParameterObject Pageable pageable,
            @AuthenticationPrincipal UserEntity currentUser) {
        return PageResponse.from(revisionService.getRevisions(guideId, pageable, currentUser));
    }

    @GetMapping("/achievements/{achievementId}/revisions")
    @AllowedSort({})
    public PageResponse<GuideRevisionResponse> getRevisionsByAchievement(
            @PathVariable String achievementId,
            @PageableDefault(size = 20) @ParameterObject Pageable pageable,
            @AuthenticationPrincipal UserEntity currentUser) {
        return PageResponse.from(revisionService.getRevisionsByAchievement(achievementId, pageable, currentUser));
    }

    @GetMapping("/revisions/{revisionId}")
    public GuideRevisionResponse getRevision(@PathVariable String revisionId, @AuthenticationPrincipal UserEntity currentUser) {
        return revisionService.getRevision(revisionId, currentUser);
    }

    @PostMapping("/guides/{guideId}/revisions")
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.CREATED)
    public GuideRevisionResponse createRevision(
            @PathVariable String guideId,
            @Valid @RequestBody CreateGuideRevisionRequest request,
            @AuthenticationPrincipal UserEntity currentUser) {
        return revisionService.createRevision(guideId, request, currentUser);
    }

    @PutMapping("/revisions/{revisionId}/upvote")
    @SecurityRequirement(name = "bearerAuth")
    public RevisionVoteResponse upvote(@PathVariable String revisionId, @AuthenticationPrincipal UserEntity currentUser) {
        return voteService.upvote(revisionId, currentUser);
    }

    @DeleteMapping("/revisions/{revisionId}/upvote")
    @SecurityRequirement(name = "bearerAuth")
    public RevisionVoteResponse removeUpvote(@PathVariable String revisionId, @AuthenticationPrincipal UserEntity currentUser) {
        return voteService.removeUpvote(revisionId, currentUser);
    }

}
