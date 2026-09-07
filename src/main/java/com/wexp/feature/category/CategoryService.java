package com.wexp.feature.category;

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
public class CategoryService {

    private final ICategoryRepository categoryRepository;
    private final IGameRepository gameRepository;

    public List<CategoryEntity> findAll() {
        return categoryRepository.findAll();
    }

    public Page<GameEntity> findGamesByCategory(String categoryPublicId, Pageable pageable) {
        if (!categoryRepository.existsByPublicId(categoryPublicId)) {
            throw new ApiException(ExceptionResponse.CategoryNotFound);
        }
        return gameRepository.findByCategoriesPublicId(categoryPublicId, pageable);
    }
}