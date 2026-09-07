package com.wexp.feature.game.dto;

import com.wexp.feature.game.GameEntity;

public record GameSummaryDto(
        String id,
        String name,
        Long steamAppId
) {
    public GameSummaryDto(GameEntity game) {
        this(game.getPublicId(), game.getName(), game.getSteamAppId());
    }
}
