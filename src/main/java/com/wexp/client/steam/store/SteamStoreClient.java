package com.wexp.client.steam.store;

import com.wexp.database.model.*;
import com.wexp.database.repository.ICategoryRepository;
import com.wexp.database.repository.IGenreRepository;
import com.wexp.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public class SteamStoreClient {

    private final SteamStore steamStore;
    private final ImageService imageService;
    private final ICategoryRepository categoryRepository;
    private final IGenreRepository genreRepository;

    private final DateTimeFormatter ptBrFormatter = DateTimeFormatter.ofPattern("d/MMM/yyyy", new Locale("pt", "BR"));
    private final Logger logger = LoggerFactory.getLogger(SteamStoreClient.class);

    @Value("${steam.images.base-url}")
    private String imagesUrl;

    public Optional<GameEntity> getGameDetails(Long steamAppId, String publicId, GameEntity existingGame) {
        try {
            JsonNode response = steamStore.getAppDetails(steamAppId);
            if (response == null || !response.has(steamAppId.toString())) return Optional.empty();

            JsonNode gameNode = response.get(steamAppId.toString());
            if (!gameNode.get("success").asBoolean()) return Optional.empty();

            JsonNode data = gameNode.get("data");
            String baseCdnUrl = imagesUrl + "/" + steamAppId + "/";

            GameEntity gameToSave = existingGame != null ? existingGame : GameEntity.builder()
                    .publicId(publicId)
                    .steamAppId(steamAppId)
                    .build();

            gameToSave.setName(data.get("name").asString());
            gameToSave.setShortDescription(data.get("short_description").asString());
            gameToSave.setDetailedDescription(data.get("detailed_description").asString());
            gameToSave.setReleaseDate(this.extractReleaseDate(data));

            JsonNode categoriesNode = data.get("categories");
            if (categoriesNode.isArray()) {
                Set<CategoryEntity> categories = new HashSet<>();
                categoriesNode.valueStream().forEach(catNode -> {
                    Long catId = catNode.get("id").asLong();
                    String name = catNode.get("description").asString();

                    CategoryEntity category = categoryRepository.findBySteamCategoryId(catId)
                            .orElseGet(() -> categoryRepository.save(CategoryEntity.builder()
                                    .steamCategoryId(catId)
                                    .name(name)
                                    .build()));

                    categories.add(category);
                });

                gameToSave.setCategories(categories);
            }

            JsonNode genresNode = data.get("genres");
            if (genresNode.isArray()) {
                Set<GenreEntity> genres = new HashSet<>();
                genresNode.valueStream().forEach(genNode -> {
                    String genId = genNode.get("id").asString();
                    String name = genNode.get("description").asString();

                    GenreEntity genre = genreRepository.findBySteamGenreId(genId)
                            .orElseGet(() -> genreRepository.save(GenreEntity.builder()
                                    .steamGenreId(genId)
                                    .name(name)
                                    .build()));

                    genres.add(genre);
                });

                gameToSave.setGenres(genres);
            }

            Map<ImageType, String> imageUrls = new EnumMap<>(ImageType.class);
            imageUrls.put(ImageType.HEADER, data.path("header_image").asString(null));
            imageUrls.put(ImageType.BACKGROUND, data.path("background_raw").asString(null));
            imageUrls.put(ImageType.CAPSULE, data.path("capsule_image").asString(null));
            imageUrls.put(ImageType.LOGO, baseCdnUrl + "logo.png");
            imageUrls.put(ImageType.COVER, baseCdnUrl + "library_600x900.jpg");
            imageUrls.put(ImageType.HERO, baseCdnUrl + "library_hero.jpg");

            imageService.processAndCacheImagesAsync(gameToSave, imageUrls);
            return Optional.of(gameToSave);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return Optional.empty();
    }

    public List<SteamGameEntity> searchGames(String query) {
        List<SteamGameEntity> results = new ArrayList<>();

        JsonNode response = steamStore.searchGames(query);
        if (response == null || !response.has("items")) return results;

        JsonNode items = response.get("items");
        if (items == null || !items.isArray()) return results;

        items.valueStream()
                .filter(item -> item.has("id") && item.has("name"))
                .forEach(item -> results.add(SteamGameEntity.builder()
                        .appid(item.get("id").asLong())
                        .name(item.get("name").asString())
                        .build()
                ));

        return results;
    }

    private LocalDateTime extractReleaseDate(JsonNode data) {
        JsonNode releaseNode = data.get("release_date");
        if (releaseNode.isMissingNode() || releaseNode.path("coming_soon").asBoolean()) return null;

        String dateString = releaseNode.path("date").asString();
        try {
            LocalDate releaseDate = LocalDate.parse(dateString, ptBrFormatter);
            return releaseDate.atStartOfDay();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }
}
