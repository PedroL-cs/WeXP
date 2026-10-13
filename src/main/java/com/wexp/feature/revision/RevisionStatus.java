package com.wexp.feature.revision;

public enum RevisionStatus {
    PUBLISHED,
    // Retained so existing rows can be read and migrated on startup.
    PENDING,
    APPROVED,
    REJECTED,
}
