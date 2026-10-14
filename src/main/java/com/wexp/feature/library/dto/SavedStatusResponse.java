package com.wexp.feature.library.dto;

import com.wexp.feature.library.SavedItemEntity;
import com.wexp.feature.library.SavedResourceType;

import java.time.LocalDateTime;

public record SavedStatusResponse(
        SavedResourceType type,
        String resourceId,
        boolean saved,
        LocalDateTime savedAt
) {
    public static SavedStatusResponse saved(SavedItemEntity item) {
        return new SavedStatusResponse(
                item.getResourceType(),
                item.getResourcePublicId(),
                true,
                item.getSavedAt()
        );
    }

    public static SavedStatusResponse notSaved(SavedResourceType type, String resourceId) {
        return new SavedStatusResponse(type, resourceId, false, null);
    }
}
