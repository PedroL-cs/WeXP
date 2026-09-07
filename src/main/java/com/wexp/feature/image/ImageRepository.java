package com.wexp.feature.image;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
    Optional<ImageEntity> findByPublicId(String publicId);
    Optional<ImageEntity> findByOwnerTypeAndOwnerIdAndVariant(String ownerType, String ownerId, String variant);
}
