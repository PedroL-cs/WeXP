package com.wexp.feature.revision;

import com.wexp.feature.user.UserEntity;
import com.wexp.feature.revision.dto.CreateGuideRevisionRequest;
import com.wexp.feature.revision.dto.GuideRevisionResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/guides")
@Tag(name = "Guide Revision", description = "Revisões de guias")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Requisição inválida",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "401", description = "Autenticação obrigatória",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse")))
})
@RequiredArgsConstructor
public class GuideRevisionController {
    private final GuideRevisionService revisionService;

    @GetMapping("/{guideId}/revisions")
    public List<GuideRevisionResponse> getRevisions(@PathVariable String guideId) {
        return revisionService.getRevisions(guideId);
    }

    @PostMapping("/{guideId}/revisions")
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.CREATED)
    public GuideRevisionResponse createRevision(
            @PathVariable String guideId,
            @Valid @RequestBody CreateGuideRevisionRequest request,
            @AuthenticationPrincipal UserEntity currentUser) {
        return revisionService.createRevision(guideId, request, currentUser);
    }

}
