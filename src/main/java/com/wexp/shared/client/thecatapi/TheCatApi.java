package com.wexp.shared.client.thecatapi;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import tools.jackson.databind.JsonNode;

@HttpExchange("/v1")
public interface TheCatApi {

    @GetExchange("/images/search")
    JsonNode getRandomCatImage();
}
