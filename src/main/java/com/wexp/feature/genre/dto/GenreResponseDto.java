package com.wexp.feature.genre.dto;

import com.wexp.feature.genre.GenreEntity;

public record GenreResponseDto(
        String id,
        String name
) {
    public GenreResponseDto(GenreEntity genre) {
        this(genre.getPublicId(), genre.getName());
    }
}
