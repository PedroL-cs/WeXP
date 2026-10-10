package com.wexp.shared.client.steam.api;

import com.wexp.feature.game.steam.SteamGameEntity;
import com.wexp.shared.client.steam.SteamRequestExecutor;
import com.wexp.shared.client.steam.SteamIntegrationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SteamApiClient {

    private final SteamApi steamApi;
    private final SteamRequestExecutor requests;

    public List<SteamGameEntity> listAllSteamGames() {
        List<SteamGameEntity> steamGames = new ArrayList<>();

        long lastAppId = 0L;
        boolean haveMoreResults = true;

        while (haveMoreResults) {
            long cursor = lastAppId;
            JsonNode response = requests.execute(SteamRequestExecutor.API, () -> {
                JsonNode page = steamApi.getAppList(cursor);
                if (page == null || !page.path("response").path("apps").isArray()) throw invalidResponse();
                JsonNode result = page.get("response");
                if (result.path("have_more_results").asBoolean(false)
                        && result.path("last_appid").asLong(0) <= cursor) throw invalidResponse();
                result.get("apps").forEach(app -> {
                    if (!app.path("appid").isNumber() || !app.path("name").isString()) throw invalidResponse();
                });
                return result;
            });
            if (!response.has("have_more_results") || !response.get("have_more_results").asBoolean()) haveMoreResults = false;
            if (response.has("last_appid")) lastAppId = response.get("last_appid").asLong();

            response.get("apps").forEach(app -> steamGames.add(SteamGameEntity.builder()
                    .appid(app.get("appid").asLong())
                    .name(app.get("name").asString())
                    .build()
            ));
        }

        return steamGames;
    }

    public JsonNode getSchemaForGame(Long steamAppId) {
        return requests.execute(SteamRequestExecutor.API, () -> {
            JsonNode schema = steamApi.getSchemaForGame(steamAppId);
            if (schema == null || !schema.path("game").isObject()) throw invalidResponse();
            JsonNode achievements = schema.path("game").path("availableGameStats").get("achievements");
            if (achievements != null) {
                if (!achievements.isArray()) throw invalidResponse();
                achievements.forEach(achievement -> {
                    if (!achievement.path("name").isString()) throw invalidResponse();
                });
            }
            return schema;
        });
    }

    private SteamIntegrationException invalidResponse() {
        return new SteamIntegrationException(SteamIntegrationException.Reason.INVALID_RESPONSE);
    }
}
