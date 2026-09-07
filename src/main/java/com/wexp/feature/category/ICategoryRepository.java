package com.wexp.feature.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ICategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findBySteamCategoryId(Long steamCategoryId);
    Optional<CategoryEntity> findByPublicId(String publicId);
    boolean existsByPublicId(String publicId);}