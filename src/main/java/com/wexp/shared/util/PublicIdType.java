package com.wexp.shared.util;

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
    REVISION("rev"),
    IMAGE("img");

    private final String prefix;
}
