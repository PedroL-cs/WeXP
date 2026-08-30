package com.wexp.client.steam.api;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import tools.jackson.databind.JsonNode;

public interface SteamApi {
    @GetExchange("/IStoreService/GetAppList/v1/")
    JsonNode getAppList(
            @RequestParam("last_appid") Long lastAppid,
            @RequestParam("max_results") Long maxResults
    );

    default JsonNode getAppList(Long lastAppid) {
        return getAppList(lastAppid, 50_000L);
    }

    @GetExchange("/ISteamUserStats/GetSchemaForGame/v2")
    JsonNode getSchemaForGame(
            @RequestParam("appid") Long appId,
            @RequestParam("l") String language
    );

    default JsonNode getSchemaForGame(Long appId) {
        return getSchemaForGame(appId, "brazilian");
    }

}
