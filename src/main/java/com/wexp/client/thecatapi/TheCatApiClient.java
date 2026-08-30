package com.wexp.client.thecatapi;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TheCatApiClient {

    private final TheCatApi theCatApi;

    public Optional<String> getRandomCatImage() {
        try {
            JsonNode response = theCatApi.getRandomCatImage();
            if (response == null || !response.isArray() || response.isEmpty()) return Optional.empty();

            JsonNode urlNode = response.get(0).get("url");
            if (urlNode == null) return Optional.empty();

            return Optional.of(urlNode.asString());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
