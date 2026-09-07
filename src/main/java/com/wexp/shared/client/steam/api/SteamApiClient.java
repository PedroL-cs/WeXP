package com.wexp.shared.client.steam.api;

import com.wexp.feature.game.steam.SteamGameEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SteamApiClient {

    private final SteamApi steamApi;

    public List<SteamGameEntity> listAllSteamGames() {
        List<SteamGameEntity> steamGames = new ArrayList<>();

        long lastAppId = 0L;
        boolean haveMoreResults = true;

        while (haveMoreResults) {
            JsonNode response = steamApi.getAppList(lastAppId);

            if (response == null || !response.has("response")) return steamGames;

            response = response.get("response");
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
        return steamApi.getSchemaForGame(steamAppId);
    }
}
