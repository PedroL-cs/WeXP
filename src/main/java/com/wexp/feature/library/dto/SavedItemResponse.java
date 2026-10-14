package com.wexp.feature.library.dto;

import com.wexp.feature.library.SavedItemEntity;
import com.wexp.feature.library.SavedResourceType;

import java.time.LocalDateTime;

public record SavedItemResponse(
        SavedResourceType type,
        String resourceId,
        LocalDateTime savedAt
) {
    public SavedItemResponse(SavedItemEntity item) {
        this(item.getResourceType(), item.getResourcePublicId(), item.getSavedAt());
    }
}
