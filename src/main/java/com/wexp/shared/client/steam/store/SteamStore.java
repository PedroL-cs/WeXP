package com.wexp.shared.client.steam.store;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import tools.jackson.databind.JsonNode;

@HttpExchange("/api")
public interface SteamStore {

    @GetExchange("/appdetails")
    JsonNode getAppDetails(
            @RequestParam("appids") Long appId,
            @RequestParam("l") String language
    );

    default JsonNode getAppDetails(Long appId) {
        return this.getAppDetails(appId, "brazilian");
    }

    @GetExchange("/storesearch")
    JsonNode searchGames(
            @RequestParam("term") String term,
            @RequestParam("l")  String language,
            @RequestParam("cc") String cc
    );

    default JsonNode searchGames(String term) {
        return this.searchGames(term, "brazilian", "BR");
    }
}
