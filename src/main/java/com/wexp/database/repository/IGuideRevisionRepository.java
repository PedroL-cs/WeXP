package com.wexp.database.repository;

import com.wexp.database.model.GuideRevisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IGuideRevisionRepository extends JpaRepository<GuideRevisionEntity, Long> {
    Optional<GuideRevisionEntity> findByPublicId(String publicId);
}
