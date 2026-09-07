package com.wexp.feature.guide;

import com.wexp.feature.user.UserEntity;
import com.wexp.feature.guide.dto.CreateGuideRequestDto;
import com.wexp.feature.guide.dto.GuideResponseDto;
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
@Tag(name = "Guide", description = "Guias de conquistas")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Requisição inválida",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "401", description = "Autenticação obrigatória",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "409", description = "Conflito de recurso",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse")))
})
@RequiredArgsConstructor
public class GuideController {

    private final GuideService guideService;

    @GetMapping("/achievements/{achievementId}/guide")
    @ResponseStatus(HttpStatus.OK)
    public GuideResponseDto getGuideByAchievement(@PathVariable String achievementId) {
        return new GuideResponseDto(guideService.getGuideByAchievementPublicId(achievementId));
    }

    @GetMapping("/guides/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GuideResponseDto getGuideByPublicId(@PathVariable String id) {
        return new GuideResponseDto(guideService.getGuideByPublicId(id));
    }

    @PostMapping("/achievements/{achievementId}/guide")
    @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.CREATED)
    public GuideResponseDto createInitialGuide(
            @PathVariable String achievementId,
            @Valid @RequestBody CreateGuideRequestDto dto,
            @AuthenticationPrincipal UserEntity currentUser) {
        return new GuideResponseDto(guideService.createInitialGuide(achievementId, dto, currentUser));
    }
}
