package com.wexp.service;

import com.wexp.database.model.GameEntity;
import com.wexp.database.model.GenreEntity;
import com.wexp.database.repository.IGameRepository;
import com.wexp.database.repository.IGenreRepository;
import com.wexp.exception.ApiException;
import com.wexp.exception.ExceptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final IGenreRepository genreRepository;
    private final IGameRepository gameRepository;

    public List<GenreEntity> findAll() {
        return genreRepository.findAll();
    }

    public Page<GameEntity> findGamesByGenre(String genrePublicId, Pageable pageable) {
        if (!genreRepository.existsByPublicId(genrePublicId)) {
            throw new ApiException(ExceptionResponse.GenreNotFound);
        }
        return gameRepository.findByGenresPublicId(genrePublicId, pageable);
    }
}