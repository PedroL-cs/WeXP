package com.wexp.feature.library;

import com.wexp.feature.library.dto.SavedItemResponse;
import com.wexp.feature.library.dto.SavedStatusResponse;
import com.wexp.feature.user.UserEntity;
import com.wexp.shared.dto.PageResponse;
import com.wexp.shared.pagination.AllowedSort;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/saved-items")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Saved items", description = "Itens salvos pelo usuário autenticado")
@RequiredArgsConstructor
public class SavedItemController {

    private final SavedItemService savedItemService;

    @GetMapping
    @AllowedSort({"savedAt"})
    public PageResponse<SavedItemResponse> list(
            @RequestParam(required = false) SavedResourceType type,
            @PageableDefault(size = 20, sort = "savedAt", direction = Sort.Direction.DESC)
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal UserEntity currentUser
    ) {
        return PageResponse.from(savedItemService.list(type, pageable, currentUser));
    }

    @PutMapping("/games/{resourceId}")
    public SavedStatusResponse saveGame(
            @PathVariable String resourceId,
            @AuthenticationPrincipal UserEntity currentUser
    ) {
        return savedItemService.save(SavedResourceType.GAME, resourceId, currentUser);
    }

    @GetMapping("/games/{resourceId}")
    public SavedStatusResponse gameStatus(
            @PathVariable String resourceId,
            @AuthenticationPrincipal UserEntity currentUser
    ) {
        return savedItemService.status(SavedResourceType.GAME, resourceId, currentUser);
    }

    @DeleteMapping("/games/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeGame(
            @PathVariable String resourceId,
            @AuthenticationPrincipal UserEntity currentUser
    ) {
        savedItemService.remove(SavedResourceType.GAME, resourceId, currentUser);
    }

    @PutMapping("/achievements/{resourceId}")
    public SavedStatusResponse saveAchievement(
            @PathVariable String resourceId,
            @AuthenticationPrincipal UserEntity currentUser
    ) {
        return savedItemService.save(SavedResourceType.ACHIEVEMENT, resourceId, currentUser);
    }

    @GetMapping("/achievements/{resourceId}")
    public SavedStatusResponse achievementStatus(
            @PathVariable String resourceId,
            @AuthenticationPrincipal UserEntity currentUser
    ) {
        return savedItemService.status(SavedResourceType.ACHIEVEMENT, resourceId, currentUser);
    }

    @DeleteMapping("/achievements/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAchievement(
            @PathVariable String resourceId,
            @AuthenticationPrincipal UserEntity currentUser
    ) {
        savedItemService.remove(SavedResourceType.ACHIEVEMENT, resourceId, currentUser);
    }

    @PutMapping("/guides/{resourceId}")
    public SavedStatusResponse saveGuide(
            @PathVariable String resourceId,
            @AuthenticationPrincipal UserEntity currentUser
    ) {
        return savedItemService.save(SavedResourceType.GUIDE, resourceId, currentUser);
    }

    @GetMapping("/guides/{resourceId}")
    public SavedStatusResponse guideStatus(
            @PathVariable String resourceId,
            @AuthenticationPrincipal UserEntity currentUser
    ) {
        return savedItemService.status(SavedResourceType.GUIDE, resourceId, currentUser);
    }

    @DeleteMapping("/guides/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeGuide(
            @PathVariable String resourceId,
            @AuthenticationPrincipal UserEntity currentUser
    ) {
        savedItemService.remove(SavedResourceType.GUIDE, resourceId, currentUser);
    }
}
