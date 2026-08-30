package com.wexp.service;

import com.wexp.database.model.GameEntity;
import com.wexp.database.model.ImageEntity;
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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ImageService {

    private final Logger logger = LoggerFactory.getLogger(ImageService.class);
    private final Path storageLocation = Paths.get("uploads/games");

    public ImageService() {
        try { Files.createDirectories(storageLocation); }
        catch (Exception e) { throw new RuntimeException("Não foi possível criar o diretório de uploads.", e); }
    }

    public ResponseEntity<Resource> getImage(String gameId, String imageType) {
        Resource file = loadImageAsResource(gameId, imageType.toLowerCase());
        if (file == null) throw new ApiException(ExceptionResponse.ImageNotFound);

        String contentType = MediaType.IMAGE_JPEG_VALUE;
        if (file.getFilename() != null && file.getFilename().endsWith(".png")) {
            contentType = MediaType.IMAGE_PNG_VALUE;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(file);
    }

    public void processAndCacheImagesAsync(GameEntity game, Map<ImageType, String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return;

        imageUrls.forEach((type, sourceUrl) -> {
            if (sourceUrl == null || sourceUrl.isBlank()) return;

            boolean exists = game.getImages().stream().anyMatch(img -> img.getType() == type);

            if (!exists) {
                ImageEntity newImg = ImageEntity.builder()
                        .game(game)
                        .type(type)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                game.addImage(newImg);
            }
        });

        CompletableFuture.runAsync(() -> downloadImagesBatch(game.getPublicId(), imageUrls));
    }

    private void downloadImagesBatch(String publicId, Map<ImageType, String> imageUrls) {
        try {
            Path gameFolder = storageLocation.resolve(publicId);
            Files.createDirectories(gameFolder);

            for (Map.Entry<ImageType, String> entry : imageUrls.entrySet()) {
                ImageType type = entry.getKey();
                String sourceUrl = entry.getValue();

                if (sourceUrl == null || sourceUrl.isBlank()) continue;

                String extension = sourceUrl.endsWith(".png") ? ".png" : ".jpg";
                String fileName = type.name().toLowerCase() + extension;
                Path targetLocation = gameFolder.resolve(fileName);

                try (InputStream in = new URL(sourceUrl).openStream()) {
                    Files.copy(in, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    logger.error("Erro no download em background da imagem [{}] para o jogo {}: {}", type, publicId, e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.warn("Erro ao criar diretório de cache para o jogo {}: {}", publicId, e.getMessage());
        }
    }

    private Resource loadImageAsResource(String publicId, String typeName) {
        try {
            Path gameFolder = storageLocation.resolve(publicId);
            Path filePath = gameFolder.resolve(typeName + ".jpg");
            if (!Files.exists(filePath)) filePath = gameFolder.resolve(typeName + ".png");

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) return resource;
        } catch (Exception e) {
            logger.error("Erro ao carregar arquivo de imagem local", e);
        }

        return null;
    }
}