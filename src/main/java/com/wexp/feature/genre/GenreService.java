package com.wexp.feature.genre;

import com.wexp.feature.game.GameEntity;
import com.wexp.feature.game.IGameRepository;
import com.wexp.shared.exception.ApiException;
import com.wexp.shared.exception.ExceptionResponse;
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