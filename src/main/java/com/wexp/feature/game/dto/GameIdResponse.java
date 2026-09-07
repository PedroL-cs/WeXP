package com.wexp.feature.game.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record GameIdResponse(Long steamId, String gameId, GameStatus gameStatus) implements Serializable {
    public enum GameStatus {
        NEW, EXISTING
    }
}
