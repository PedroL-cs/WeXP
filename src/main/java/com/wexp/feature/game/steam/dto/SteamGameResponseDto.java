package com.wexp.feature.game.steam.dto;

import com.wexp.feature.game.steam.SteamGameEntity;

public record SteamGameResponseDto(
        Long appid,
        String name,
        String acronym,
        String capsuleUrl
) {
    public SteamGameResponseDto(SteamGameEntity game) {
        this(game.getAppid(), game.getName(), game.getAcronym(), game.getCapsuleUrl());
    }
}
