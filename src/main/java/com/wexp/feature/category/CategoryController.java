package com.wexp.feature.category;

import com.wexp.feature.game.dto.GameResponseDto;
import com.wexp.feature.category.dto.CategoryResponseDto;
import com.wexp.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springdoc.core.annotations.ParameterObject;
import com.wexp.shared.pagination.AllowedSort;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Category", description = "Categorias e jogos relacionados")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Requisição inválida",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse")))
})
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryResponseDto> getAllCategories() {
        return categoryService.findAll().stream().map(CategoryResponseDto::new).toList();
    }

    @GetMapping("/{id}/games")
    @ResponseStatus(HttpStatus.OK)
    @AllowedSort({"name", "releaseDate", "viewsCount", "steamAppId"})
    public PageResponse<GameResponseDto> getGamesByCategory(
            @PathVariable String id,
            @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
        return PageResponse.from(categoryService.findGamesByCategory(id, pageable).map(GameResponseDto::new));
    }
}