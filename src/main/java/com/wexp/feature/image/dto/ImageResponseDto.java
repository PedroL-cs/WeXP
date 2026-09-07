package com.wexp.feature.image.dto;

import com.wexp.feature.image.ImageEntity;

import java.time.LocalDateTime;

public record ImageResponseDto(
        String id,
        String ownerType,
        String ownerId,
        String variant,
        String originalFilename,
        String extension,
        String contentType,
        Long size,
        String url,
        LocalDateTime createdAt
) {
    public ImageResponseDto(ImageEntity image) {
        this(
                image.getPublicId(),
                image.getOwnerType(),
                image.getOwnerId(),
                image.getVariant(),
                image.getOriginalFilename(),
                image.getExtension(),
                image.getContentType(),
                image.getSize(),
                "/api/v1/images/" + image.getPublicId(),
                image.getCreatedAt()
        );
    }
}
