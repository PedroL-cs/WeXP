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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.InputStream;
import java.net.MalformedURLException;
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

    private static final Path gamesStorageLocation = Paths.get("uploads/games");
    private static final Path achievementsStorageLocation = Paths.get("uploads/achievements");
    private static final Path avatarsStorageLocation = Paths.get("uploads/avatars");

    public ImageService() {
        try {
            Files.createDirectories(ImageService.gamesStorageLocation);
            Files.createDirectories(ImageService.achievementsStorageLocation);
            Files.createDirectories(ImageService.avatarsStorageLocation);
        } catch (Exception e) {
            logger.error("Erro ao inicializar diretórios de armazenamento de imagens", e);
            throw new RuntimeException("Não foi possível criar os diretórios de uploads.", e);
        }
    }

    public void saveUserAvatar(String userPublicId, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ApiException(ExceptionResponse.InvalidInput);

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new ApiException(ExceptionResponse.InvalidInput);
        }

        String extension = contentType.equals("image/jpeg") ? ".jpg" : ".png";

        try {
            Files.deleteIfExists(avatarsStorageLocation.resolve(userPublicId + ".png"));
            Files.deleteIfExists(avatarsStorageLocation.resolve(userPublicId + ".png"));

            Path targetLocation = avatarsStorageLocation.resolve(userPublicId + extension);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            logger.error("Erro ao salvar avatar do usuário {} {}", userPublicId, e.getMessage());
            throw new ApiException(ExceptionResponse.InternalServerError);
        }
    }

    public ResponseEntity<Resource> getUserAvatar(String userPublicId) {
        Resource file = loadUserAvatarResource(userPublicId);
        if (file == null) throw new ApiException(ExceptionResponse.ImageNotFound);
        return buildImageResponseEntity(file);
    }

    public static String getAvatarUrl(String userPublicId) {
        if (userPublicId == null) return null;

        boolean exists = Files.exists(avatarsStorageLocation.resolve(userPublicId + ".png")) ||
                Files.exists(avatarsStorageLocation.resolve(userPublicId + ".jpg"));

        String baseUrl;
        try { baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString(); }
        catch (Exception e) { baseUrl = "http://localhost:8080"; }

        return exists ? baseUrl +  "/api/v1/images/users/" + userPublicId : null;
    }

    private Resource getImageResource(Path storageLocation, String name) throws MalformedURLException {
        Path pngPath = storageLocation.resolve(name + ".png");
        if (Files.exists(pngPath)) return new UrlResource(pngPath.toUri());

        Path jpgPath = storageLocation.resolve(name + ".jpg");
        if (Files.exists(jpgPath)) return new UrlResource(jpgPath.toUri());
        return null;
    }

    private Resource loadUserAvatarResource(String userPublicId) {
        if (userPublicId == null || userPublicId.isEmpty()) return null;

        try {
            return getImageResource(avatarsStorageLocation, userPublicId);
        } catch (Exception e) {
            logger.error("Erro ao carregar avatar do usuário {}: {}", userPublicId, e.getMessage());
        }

        return null;
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
            return getImageResource(gamesStorageLocation, gamePublicId + "_" + imageType.toLowerCase());
        } catch (Exception e) {
            logger.error("Erro ao carregar imagem do jogo {}: {}", gamePublicId, e.getMessage());
        }
        return null;
    }

    private Resource loadAchievementIconResource(String achievementPublicId) {
        if (achievementPublicId == null) return null;

        try {
            return getImageResource(achievementsStorageLocation,  achievementPublicId);
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