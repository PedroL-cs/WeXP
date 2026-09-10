package com.wexp.feature.revision;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IGuideRevisionRepository extends JpaRepository<GuideRevisionEntity, Long> {
    Optional<GuideRevisionEntity> findByPublicId(String publicId);
    List<GuideRevisionEntity> findAllByGuide_PublicIdOrderByCreatedAtDesc(String guidePublicId);
}
