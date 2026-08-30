package com.wexp.controller;

import com.wexp.database.model.CategoryEntity;
import com.wexp.database.model.GameEntity;
import com.wexp.dto.PageResponse;
import com.wexp.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryEntity> getAllCategories() {
        return categoryService.findAll();
    }

    @GetMapping("/{publicId}/games")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<GameEntity> getGamesByCategory(
            @PathVariable String publicId,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(categoryService.findGamesByCategory(publicId, pageable));
    }
}