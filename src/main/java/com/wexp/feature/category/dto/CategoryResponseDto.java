package com.wexp.feature.category.dto;

import com.wexp.feature.category.CategoryEntity;

public record CategoryResponseDto(
        String id,
        String name
) {
    public CategoryResponseDto(CategoryEntity category) {
        this(category.getPublicId(), category.getName());
    }
}
