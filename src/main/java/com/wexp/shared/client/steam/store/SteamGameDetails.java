package com.wexp.shared.client.steam.store;

import tools.jackson.databind.JsonNode;

import java.util.Map;

public record SteamGameDetails(
        Long appId,
        JsonNode data,
        JsonNode achievementSchema,
        Map<String, Double> globalUnlockPercentages,
        boolean globalUnlockPercentagesFetched
) {
    public SteamGameDetails(Long appId, JsonNode data, JsonNode achievementSchema) {
        this(appId, data, achievementSchema, Map.of(), false);
    }

    public SteamGameDetails(
            Long appId,
            JsonNode data,
            JsonNode achievementSchema,
            Map<String, Double> globalUnlockPercentages
    ) {
        this(appId, data, achievementSchema, globalUnlockPercentages, true);
    }

    public SteamGameDetails {
        globalUnlockPercentages = globalUnlockPercentages == null
                ? Map.of()
                : Map.copyOf(globalUnlockPercentages);
    }
}
