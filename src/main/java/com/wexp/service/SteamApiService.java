package com.wexp.service;

import com.wexp.client.steam.api.SteamApiClient;
import com.wexp.database.model.SteamGameEntity;
import com.wexp.database.repository.ISteamGameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SteamApiService {

    private final ISteamGameRepository steamGameRepository;
    private final SteamApiClient steamApiClient;

    @Value("${steam.capsule-image-base-url}")
    private String baseCapsuleUrl;

    public Page<SteamGameEntity> instantSearchGame(String query, Pageable pageable) {
        if (steamGameRepository.findFirstBy().isEmpty()) importAllSteamGames();

        String cleanQuery = query != null ? query.replaceAll("[^a-zA-Z0-9]", "") : "";

        Page<SteamGameEntity> page = steamGameRepository.searchByNameOrAcronym(query, cleanQuery, pageable);

        String formattedBaseUrl = baseCapsuleUrl.endsWith("/") ? baseCapsuleUrl : baseCapsuleUrl + "/";
        page.forEach(game -> game.setCapsuleUrl(formattedBaseUrl + game.getAppid() + "/capsule_231x87.jpg"));

        return page;
    }

    private void importAllSteamGames() {
        List<SteamGameEntity> steamGames = steamApiClient.listAllSteamGames();
        steamGameRepository.saveAll(steamGames);
    }
}
