package com.wexp.shared.client.steam.store;

import tools.jackson.databind.JsonNode;

public record SteamGameDetails(Long appId, JsonNode data, JsonNode achievementSchema) {
}
