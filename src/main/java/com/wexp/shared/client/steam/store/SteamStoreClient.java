package com.wexp.shared.client.steam.store;

import com.wexp.shared.client.steam.api.SteamApiClient;
import com.wexp.feature.achievement.AchievementEntity;
import com.wexp.feature.category.CategoryEntity;
import com.wexp.feature.category.ICategoryRepository;
import com.wexp.feature.game.GameEntity;
import com.wexp.feature.game.steam.SteamGameEntity;
import com.wexp.feature.genre.IGenreRepository;
import com.wexp.feature.genre.GenreEntity;
import com.wexp.feature.image.ImageService;
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
            GameEntity gameToSave = this.extractGameEntity(publicId, steamAppId, data, existingGame);

            this.processCategories(data, gameToSave);
            this.processGenres(data, gameToSave);
            this.processAchievements(steamAppId, gameToSave);
            this.processImages(steamAppId, data, gameToSave);

            return Optional.of(gameToSave);
        } catch (Exception e) {
            logger.error("Erro ao consultar os detalhes do jogo na Steam", e);
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

    private GameEntity extractGameEntity(String publicId, Long steamAppId, JsonNode data, GameEntity existingGame) {
        GameEntity gameToSave = existingGame != null ? existingGame : GameEntity.builder()
                .publicId(publicId)
                .steamAppId(steamAppId)
                .build();

        gameToSave.setName(data.get("name").asString());
        gameToSave.setShortDescription(data.get("short_description").asString());
        gameToSave.setDetailedDescription(data.get("detailed_description").asString());
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

    private void processAchievements(Long steamAppId, GameEntity gameToSave) {
        try {
            JsonNode schema = steamApiClient.getSchemaForGame(steamAppId);
            if (schema == null || !schema.has("game")) return;

            JsonNode stats = schema.get("game").path("availableGameStats");
            if (!stats.has("achievements")) return;

            JsonNode achievementsNode = stats.get("achievements");
            if (!achievementsNode.isArray()) return;

            achievementsNode.valueStream().forEach(achNode -> {
                String achName = achNode.has("displayName") ? achNode.get("displayName").asString() : achNode.get("name").asString();
                String description = achNode.path("description").asString("");
                String iconUrl = achNode.path("icon").asString(null);
                boolean isHidden = achNode.path("hidden").asInt(0) == 1;

                Optional<AchievementEntity> existingAch = gameToSave.getAchievements().stream()
                        .filter(a -> a.getName().equalsIgnoreCase(achName))
                        .findFirst();

                AchievementEntity achievement;
                if (existingAch.isPresent()) {
                    achievement = existingAch.get();
                    achievement.setDescription(description);
                    if (achievement.getPublicId() == null) {
                        achievement.setPublicId(PublicIdGenerator.generate(PublicIdType.ACHIEVEMENT));
                    }
                } else {
                    achievement = AchievementEntity.builder()
                            .publicId(PublicIdGenerator.generate(PublicIdType.ACHIEVEMENT))
                            .name(achName)
                            .description(description)
                            .isHidden(isHidden)
                            .build();
                    gameToSave.addAchievement(achievement);
                }

                if (iconUrl != null && !iconUrl.isBlank()) {
                    imageService.replaceFromUrlAsync("achievement", achievement.getPublicId(), "icon", iconUrl);
                }
            });

        } catch (Exception e) {
            logger.warn("Jogo sem conquistas ativas ou erro ao buscar esquemas da Steam (AppID {}): {}", steamAppId, e.getMessage());
        }
    }

    private void processImages(Long steamAppId, JsonNode data, GameEntity gameToSave) {
        String baseCdnUrl = imagesUrl + "/" + steamAppId + "/";
        Map<String, String> imageUrls = new LinkedHashMap<>();

        imageUrls.put("header", data.path("header_image").asString(null));
        imageUrls.put("background", data.path("background_raw").asString(null));
        imageUrls.put("capsule", data.path("capsule_image").asString(null));
        imageUrls.put("logo", baseCdnUrl + "logo.png");
        imageUrls.put("cover", baseCdnUrl + "library_600x900.jpg");
        imageUrls.put("hero", baseCdnUrl + "library_hero.jpg");

        imageService.replaceFromUrlsAsync("game", gameToSave.getPublicId(), imageUrls);
    }

    private LocalDateTime extractReleaseDate(JsonNode data) {
        JsonNode releaseNode = data.get("release_date");
        if (releaseNode == null || releaseNode.isMissingNode() || releaseNode.path("coming_soon").asBoolean()) return null;

        String dateString = releaseNode.path("date").asString();
        try {
            LocalDate releaseDate = LocalDate.parse(dateString, ptBrFormatter);
            return releaseDate.atStartOfDay();
        } catch (Exception e) {
            logger.error("Erro ao interpretar a data de lançamento recebida da Steam", e);
        }

        return null;
    }
}