package com.wexp.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PublicIdType {
    USER("usr"),
    GAME("gam"),
    GENRE("gen"),
    CATEGORY("cat"),
    ACHIEVEMENT("ach"),
    GUIDE("gui"),
    REVISION("rev");

    private final String prefix;
}
