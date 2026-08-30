package com.wexp.service;

import com.wexp.database.model.GameEntity;
import com.wexp.database.model.ImageType;
import com.wexp.exception.ApiException;
import com.wexp.exception.ExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    private final Path gamesStorageLocation = Paths.get("uploads/games");
    private final Path achievementsStorageLocation = Paths.get("uploads/achievements");

    public ImageService() {
        try {
            Files.createDirectories(this.gamesStorageLocation);
            Files.createDirectories(this.achievementsStorageLocation);
        } catch (Exception e) {
            logger.error("Erro ao inicializar diretórios de armazenamento de imagens", e);
            throw new RuntimeException("Não foi possível criar os diretórios de uploads.", e);
        }
    }

    public void processAndCacheImagesAsync(GameEntity game, Map<ImageType, String> imageUrls) {
        if (game == null || game.getPublicId() == null || imageUrls == null || imageUrls.isEmpty()) return;

        CompletableFuture.runAsync(() -> imageUrls.forEach((type, url) -> {
            if (url != null && !url.isBlank()) {
                downloadGameImage(game.getPublicId(), type, url);
            }
        }));
    }

    public void downloadAchievementIconAsync(String achievementPublicId, String sourceUrl) {
        if (achievementPublicId == null || achievementPublicId.isBlank() || sourceUrl == null || sourceUrl.isBlank()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String extension = sourceUrl.toLowerCase().contains(".png") ? ".png" : ".jpg";
                Path targetLocation = achievementsStorageLocation.resolve(achievementPublicId + extension);

                if (Files.exists(targetLocation)) return;

                try (InputStream in = URI.create(sourceUrl).toURL().openStream()) {
                    Files.copy(in, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                logger.error("Erro ao baixar ícone da conquista {}: {}", achievementPublicId, e.getMessage());
            }
        });
    }

    public ResponseEntity<Resource> getImage(String gamePublicId, String imageType) {
        Resource file = loadGameImageResource(gamePublicId, imageType);
        if (file == null) {
            throw new ApiException(ExceptionResponse.ImageNotFound);
        }
        return buildImageResponseEntity(file);
    }

    public ResponseEntity<Resource> getAchievementIcon(String achievementPublicId) {
        Resource file = loadAchievementIconResource(achievementPublicId);
        if (file == null) {
            throw new ApiException(ExceptionResponse.ImageNotFound);
        }
        return buildImageResponseEntity(file);
    }

    private void downloadGameImage(String gamePublicId, ImageType type, String sourceUrl) {
        if (gamePublicId == null || gamePublicId.isBlank() || sourceUrl == null || sourceUrl.isBlank()) return;

        try {
            String extension = sourceUrl.toLowerCase().contains(".png") ? ".png" : ".jpg";
            Path targetLocation = gamesStorageLocation.resolve(gamePublicId + "_" + type.name().toLowerCase() + extension);

            if (Files.exists(targetLocation)) return;

            try (InputStream in = URI.create(sourceUrl).toURL().openStream()) {
                Files.copy(in, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            logger.error("Erro ao baixar imagem [{}] para o jogo {}: {}", type, gamePublicId, e.getMessage());
        }
    }

    private Resource loadGameImageResource(String gamePublicId, String imageType) {
        if (gamePublicId == null || imageType == null) return null;

        try {
            Path pngPath = gamesStorageLocation.resolve(gamePublicId + "_" + imageType.toLowerCase() + ".png");
            if (Files.exists(pngPath)) {
                return new UrlResource(pngPath.toUri());
            }

            Path jpgPath = gamesStorageLocation.resolve(gamePublicId + "_" + imageType.toLowerCase() + ".jpg");
            if (Files.exists(jpgPath)) {
                return new UrlResource(jpgPath.toUri());
            }
        } catch (Exception e) {
            logger.error("Erro ao carregar imagem do jogo {}: {}", gamePublicId, e.getMessage());
        }
        return null;
    }

    private Resource loadAchievementIconResource(String achievementPublicId) {
        if (achievementPublicId == null) return null;

        try {
            Path pngPath = achievementsStorageLocation.resolve(achievementPublicId + ".png");
            if (Files.exists(pngPath)) {
                return new UrlResource(pngPath.toUri());
            }

            Path jpgPath = achievementsStorageLocation.resolve(achievementPublicId + ".jpg");
            if (Files.exists(jpgPath)) {
                return new UrlResource(jpgPath.toUri());
            }
        } catch (Exception e) {
            logger.error("Erro ao carregar ícone da conquista {}: {}", achievementPublicId, e.getMessage());
        }
        return null;
    }

    private ResponseEntity<Resource> buildImageResponseEntity(Resource resource) {
        String filename = resource.getFilename();
        String contentType = MediaType.IMAGE_JPEG_VALUE;

        if (filename != null && filename.toLowerCase().endsWith(".png")) {
            contentType = MediaType.IMAGE_PNG_VALUE;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(resource);
    }
}