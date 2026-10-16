package com.wexp.feature.game.steam;

import com.wexp.shared.client.steam.api.SteamApiClient;
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
    private final Object catalogImportLock = new Object();

    @Value("${steam.capsule-image-base-url}")
    private String baseCapsuleUrl;

    public Page<SteamGameEntity> instantSearchGame(String query, Pageable pageable) {
        ensureCatalogImported();

        String cleanQuery = query != null ? query.replaceAll("[^a-zA-Z0-9]", "") : "";

        Page<SteamGameEntity> page = steamGameRepository.searchByNameOrAcronym(query, cleanQuery, pageable);

        String formattedBaseUrl = baseCapsuleUrl.endsWith("/") ? baseCapsuleUrl : baseCapsuleUrl + "/";
        page.forEach(game -> game.setCapsuleUrl(formattedBaseUrl + game.getAppid() + "/capsule_231x87.jpg"));

        return page;
    }

    private void ensureCatalogImported() {
        synchronized (catalogImportLock) {
            if (steamGameRepository.findFirstBy().isPresent()) return;

            List<SteamGameEntity> steamGames = steamApiClient.listAllSteamGames();
            steamGameRepository.saveAll(steamGames);
        }
    }
}
