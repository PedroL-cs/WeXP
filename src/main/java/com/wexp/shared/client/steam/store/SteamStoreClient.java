package com.wexp.shared.client.steam.store;

import com.wexp.shared.client.steam.api.SteamApiClient;
import com.wexp.shared.client.steam.SteamIntegrationException;
import com.wexp.shared.client.steam.SteamRequestExecutor;
import com.wexp.feature.achievement.AchievementEntity;
import com.wexp.feature.category.CategoryEntity;
import com.wexp.feature.category.ICategoryRepository;
import com.wexp.feature.game.GameEntity;
import com.wexp.feature.game.steam.SteamGameEntity;
import com.wexp.feature.genre.IGenreRepository;
import com.wexp.feature.genre.GenreEntity;
import com.wexp.shared.util.PublicIdGenerator;
import com.wexp.shared.util.PublicIdType;
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
    private final SteamApiClient steamApiClient;
    private final SteamRequestExecutor requests;
    private final ICategoryRepository categoryRepository;
    private final IGenreRepository genreRepository;

    private final DateTimeFormatter ptBrFormatter = DateTimeFormatter.ofPattern("d/MMM/yyyy", new Locale("pt", "BR"));
    private final Logger logger = LoggerFactory.getLogger(SteamStoreClient.class);

    @Value("${steam.images.base-url}")
    private String imagesUrl;

    public Optional<SteamGameDetails> fetchGameDetails(Long steamAppId) {
        JsonNode data = requests.execute(SteamRequestExecutor.STORE, () -> {
            JsonNode response = steamStore.getAppDetails(steamAppId);
            if (response == null || !response.has(steamAppId.toString())) throw invalidResponse();
            JsonNode gameNode = response.get(steamAppId.toString());
            if (!gameNode.path("success").isBoolean()) throw invalidResponse();
            if (!gameNode.get("success").asBoolean()) return null;
            JsonNode details = gameNode.get("data");
            if (details == null || !details.isObject() || !details.path("name").isString()
                    || details.path("name").asString().isBlank()) throw invalidResponse();
            validateCollection(details, "categories", "id", "description");
            validateCollection(details, "genres", "id", "description");
            return details;
        });
        if (data == null) return Optional.empty();
        JsonNode schema = steamApiClient.getSchemaForGame(steamAppId);
        return Optional.of(new SteamGameDetails(steamAppId, data, schema));
    }

    // No network calls here: this method is used within the persistence transaction.
    public GameEntity applyDetails(SteamGameDetails details, String publicId, GameEntity existingGame) {
        GameEntity game = extractGameEntity(publicId, details.appId(), details.data(), existingGame);
        processCategories(details.data(), game);
        processGenres(details.data(), game);
        processAchievements(details.achievementSchema(), game);
        processImages(details.appId(), details.data(), game);
        return game;
    }

    public List<SteamGameEntity> searchGames(String query) {
        List<SteamGameEntity> results = new ArrayList<>();

        JsonNode items = requests.execute(SteamRequestExecutor.STORE, () -> {
            JsonNode response = steamStore.searchGames(query);
            if (response == null || !response.path("items").isArray()) throw invalidResponse();
            validateCollection(response, "items", "id", "name");
            return response.get("items");
        });

        items.valueStream()
                .filter(item -> item.has("id") && item.has("name"))
                .forEach(item -> results.add(SteamGameEntity.builder()
                        .appid(item.get("id").asLong())
                        .name(item.get("name").asString())
                        .build()
                ));

        return results;
    }

    private GameEntity extractGameEntity(String publicId, Long steamAppId, JsonNode data, GameEntity existingGame) {
        GameEntity gameToSave = existingGame != null ? existingGame : GameEntity.builder()
                .publicId(publicId)
                .steamAppId(steamAppId)
                .build();

        gameToSave.setName(data.get("name").asString());
        gameToSave.setShortDescription(data.path("short_description").asString(""));
        gameToSave.setDetailedDescription(data.path("detailed_description").asString(""));
        gameToSave.setReleaseDate(this.extractReleaseDate(data));

        return gameToSave;
    }

    private void processCategories(JsonNode data, GameEntity gameToSave) {
        JsonNode categoriesNode = data.get("categories");
        if (categoriesNode != null && categoriesNode.isArray()) {
            Set<CategoryEntity> newCategories = new HashSet<>();
            categoriesNode.valueStream().forEach(catNode -> {
                Long catId = catNode.get("id").asLong();
                String name = catNode.get("description").asString();

                CategoryEntity category = categoryRepository.findBySteamCategoryId(catId)
                        .orElseGet(() -> categoryRepository.save(CategoryEntity.builder()
                                .steamCategoryId(catId)
                                .name(name)
                                .build()));

                newCategories.add(category);
            });

            if (gameToSave.getCategories() == null) {
                gameToSave.setCategories(newCategories);
            } else {
                gameToSave.getCategories().retainAll(newCategories);
                gameToSave.getCategories().addAll(newCategories);
            }
        }
    }

    private void processGenres(JsonNode data, GameEntity gameToSave) {
        JsonNode genresNode = data.get("genres");
        if (genresNode != null && genresNode.isArray()) {
            Set<GenreEntity> newGenres = new HashSet<>();
            genresNode.valueStream().forEach(genNode -> {
                String genId = genNode.get("id").asString();
                String name = genNode.get("description").asString();

                GenreEntity genre = genreRepository.findBySteamGenreId(genId)
                        .orElseGet(() -> genreRepository.save(GenreEntity.builder()
                                .steamGenreId(genId)
                                .name(name)
                                .build()));

                newGenres.add(genre);
            });

            if (gameToSave.getGenres() == null) {
                gameToSave.setGenres(newGenres);
            } else {
                gameToSave.getGenres().retainAll(newGenres);
                gameToSave.getGenres().addAll(newGenres);
            }
        }
    }

    private void processAchievements(JsonNode schema, GameEntity gameToSave) {
        if (schema == null || !schema.has("game")) return;
        JsonNode achievements = schema.get("game").path("availableGameStats").path("achievements");
        if (!achievements.isArray()) return;

        achievements.forEach(node -> {
            String name = node.has("displayName") ? node.get("displayName").asString() : node.get("name").asString();
            String description = node.path("description").asString("");
            String iconUrl = textOrNull(node, "icon");
            boolean hidden = node.path("hidden").asInt(0) == 1;
            Optional<AchievementEntity> existing = gameToSave.getAchievements().stream()
                    .filter(a -> a.getName().equalsIgnoreCase(name)).findFirst();
            if (existing.isPresent()) {
                AchievementEntity achievement = existing.get();
                achievement.setDescription(description);
                achievement.setIconUrl(iconUrl);
                achievement.setIsHidden(hidden);
                if (achievement.getPublicId() == null) {
                    achievement.setPublicId(PublicIdGenerator.generate(PublicIdType.ACHIEVEMENT));
                }
            } else {
                gameToSave.addAchievement(AchievementEntity.builder()
                        .publicId(PublicIdGenerator.generate(PublicIdType.ACHIEVEMENT))
                        .name(name).description(description).iconUrl(iconUrl).isHidden(hidden).build());
            }
        });
    }

    private void validateCollection(JsonNode parent, String field, String id, String label) {
        JsonNode collection = parent.get(field);
        if (collection == null) return;
        if (!collection.isArray()) throw invalidResponse();
        collection.forEach(item -> {
            if (item.get(id) == null || item.get(id).isNull() || !item.path(label).isString()) throw invalidResponse();
            if ("categories".equals(field) && !item.get(id).isIntegralNumber()) throw invalidResponse();
            if ("genres".equals(field) && !item.get(id).isString() && !item.get(id).isIntegralNumber()) throw invalidResponse();
        });
    }

    private SteamIntegrationException invalidResponse() {
        return new SteamIntegrationException(SteamIntegrationException.Reason.INVALID_RESPONSE);
    }

    private void processImages(Long steamAppId, JsonNode data, GameEntity gameToSave) {
        String baseCdnUrl = imagesUrl + "/" + steamAppId + "/";
        gameToSave.setHeaderImageUrl(textOrNull(data, "header_image"));
        gameToSave.setBackgroundImageUrl(textOrNull(data, "background_raw"));
        gameToSave.setCapsuleImageUrl(textOrNull(data, "capsule_image"));
        gameToSave.setLogoImageUrl(baseCdnUrl + "logo.png");
        gameToSave.setCoverImageUrl(baseCdnUrl + "library_600x900.jpg");
        gameToSave.setHeroImageUrl(baseCdnUrl + "library_hero.jpg");
    }

    private String textOrNull(JsonNode node, String field) {
        String value = node.path(field).asString(null);
        return value == null || value.isBlank() ? null : value;
    }

    private LocalDateTime extractReleaseDate(JsonNode data) {
        JsonNode releaseNode = data.get("release_date");
        if (releaseNode == null || releaseNode.isMissingNode() || releaseNode.path("coming_soon").asBoolean(false)) return null;

        String dateString = releaseNode.path("date").asString("");
        try {
            LocalDate releaseDate = LocalDate.parse(dateString, ptBrFormatter);
            return releaseDate.atStartOfDay();
        } catch (Exception e) {
            logger.error("Erro ao interpretar a data de lançamento recebida da Steam", e);
        }

        return null;
    }
}
