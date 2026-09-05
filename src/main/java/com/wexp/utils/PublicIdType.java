package com.wexp.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PublicIdType {
    GAME("g"),
    ACHIEVEMENT("a"),
    CATEGORY("c"),
    GENRE("r"),
    USER("u");

    private final String prefix;
}
