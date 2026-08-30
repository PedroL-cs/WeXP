package com.wexp.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PublicIdType {
    GAME("g"),
    IMAGE("i"),
    CATEGORY("c"),
    GENRE("r"),;

    private final String prefix;
}
