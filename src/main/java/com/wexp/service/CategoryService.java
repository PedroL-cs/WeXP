package com.wexp.service;

import com.wexp.database.model.CategoryEntity;
import com.wexp.database.model.GameEntity;
import com.wexp.database.repository.ICategoryRepository;
import com.wexp.database.repository.IGameRepository;
import com.wexp.exception.ApiException;
import com.wexp.exception.ExceptionResponse;
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