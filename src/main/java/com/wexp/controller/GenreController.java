package com.wexp.controller;

import com.wexp.database.model.GameEntity;
import com.wexp.database.model.GenreEntity;
import com.wexp.dto.PageResponse;
import com.wexp.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<GenreEntity> getAllGenres() {
        return genreService.findAll();
    }

    @GetMapping("/{publicId}/games")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<GameEntity> getGamesByGenre(
            @PathVariable String publicId,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(genreService.findGamesByGenre(publicId, pageable));
    }
}