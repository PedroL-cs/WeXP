package com.wexp.feature.genre;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IGenreRepository extends JpaRepository<GenreEntity, Long> {
    Optional<GenreEntity> findBySteamGenreId(String steamGenreId);
    Optional<GenreEntity> findByPublicId(String publicId);
    boolean existsByPublicId(String publicId);}