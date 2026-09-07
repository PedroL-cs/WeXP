package com.wexp.feature.image;

import com.wexp.shared.exception.ApiException;
import com.wexp.shared.exception.ExceptionResponse;
import com.wexp.shared.util.PublicIdGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String IMAGE_WEBP = "image/webp";

    private final ImageRepository imageRepository;
    private final ImageStorage imageStorage;

    @Value("${images.max-size-bytes:10485760}")
    private long maxSizeBytes;

    public ImageEntity replace(String ownerType, String ownerId, String variant, MultipartFile file) {
        validateOwner(ownerType, ownerId, variant);
        validate(file);

        String extension = extensionFor(file.getContentType(), file.getOriginalFilename());
        String publicId = PublicIdGenerator.generate(com.wexp.shared.util.PublicIdType.IMAGE);
        String storageKey = resolvePath(ownerType, ownerId, variant, publicId, extension);
        ImageEntity previousImage = imageRepository
                .findByOwnerTypeAndOwnerIdAndVariant(ownerType, ownerId, variant)
                .orElse(null);
        boolean reusesStoragePath = previousImage != null
                && storageKey.equals(previousImage.getStorageKey());

        try {
            try (InputStream input = file.getInputStream()) {
                imageStorage.store(storageKey, input);
            }

            ImageEntity image = ImageEntity.builder()
                    .publicId(publicId)
                    .ownerType(ownerType)
                    .ownerId(ownerId)
                    .variant(variant)
                    .originalFilename(file.getOriginalFilename())
                    .extension(extension)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .storageKey(storageKey)
                    .build();
            if (reusesStoragePath) {
                imageRepository.delete(previousImage);
                imageRepository.flush();
            }
            ImageEntity savedImage = imageRepository.save(image);
            if (!reusesStoragePath) {
                removePrevious(previousImage);
            }
            return savedImage;
        } catch (Exception exception) {
            imageStorage.delete(storageKey);
            logger.error("Erro ao armazenar imagem {}:{}/{}", ownerType, ownerId, variant, exception);
            throw new ApiException(ExceptionResponse.InternalServerError);
        }
    }

    public void replaceFromUrlAsync(String ownerType, String ownerId, String variant, String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isBlank()) return;
        ImageEntity existingImage = imageRepository
                .findByOwnerTypeAndOwnerIdAndVariant(ownerType, ownerId, variant)
                .orElse(null);
        if (existingImage != null && imageStorage.load(existingImage.getStorageKey()) != null) return;

        CompletableFuture.runAsync(() -> {
            try (InputStream input = URI.create(sourceUrl).toURL().openStream()) {
                replaceFromStream(ownerType, ownerId, variant, sourceUrl, input);
            } catch (Exception exception) {
                logger.warn("Erro ao armazenar imagem remota {}:{}/{}", ownerType, ownerId, variant, exception);
            }
        });
    }

    public void replaceFromUrlsAsync(String ownerType, String ownerId, Map<String, String> sources) {
        if (sources == null || sources.isEmpty()) return;
        sources.forEach((variant, sourceUrl) -> replaceFromUrlAsync(ownerType, ownerId, variant, sourceUrl));
    }

    public ResponseEntity<Resource> get(String publicId) {
        ImageEntity image = imageRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.ImageNotFound));
        return response(image, loadResource(image));
    }

    public ResponseEntity<Resource> get(String ownerType, String ownerId, String variant) {
        validateOwner(ownerType, ownerId, variant);
        ImageEntity image = imageRepository.findByOwnerTypeAndOwnerIdAndVariant(ownerType, ownerId, variant)
                .orElseThrow(() -> new ApiException(ExceptionResponse.ImageNotFound));
        return response(image, loadResource(image));
    }

    public void delete(String publicId) {
        ImageEntity image = imageRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ApiException(ExceptionResponse.ImageNotFound));
        deleteFile(image);
        imageRepository.delete(image);
    }

    public static String url(String ownerType, String ownerId, String variant) {
        if (ownerId == null) return null;
        String baseUrl;
        try {
            baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        } catch (IllegalStateException exception) {
            baseUrl = DEFAULT_BASE_URL;
        }
        return baseUrl + "/api/v1/images/" + ownerType + "/" + ownerId + "/" + variant;
    }

    private void replaceFromStream(
            String ownerType,
            String ownerId,
            String variant,
            String sourceName,
            InputStream input
    ) throws Exception {
        validateOwner(ownerType, ownerId, variant);
        String extension = extensionFor(null, sourceName);
        String publicId = com.wexp.shared.util.PublicIdGenerator.generate(com.wexp.shared.util.PublicIdType.IMAGE);
        String storageKey = resolvePath(ownerType, ownerId, variant, publicId, extension);
        ImageEntity previousImage = imageRepository
                .findByOwnerTypeAndOwnerIdAndVariant(ownerType, ownerId, variant)
                .orElse(null);
        boolean reusesStoragePath = previousImage != null
                && storageKey.equals(previousImage.getStorageKey());
        imageStorage.store(storageKey, input);
        long size = imageStorage.size(storageKey);
        try {
            validateSize(size);
        } catch (ApiException exception) {
            imageStorage.delete(storageKey);
            throw exception;
        }

        ImageEntity image = ImageEntity.builder()
                .publicId(publicId)
                .ownerType(ownerType)
                .ownerId(ownerId)
                .variant(variant)
                .originalFilename(sourceName)
                .extension(extension)
                .contentType(contentTypeFor(extension))
                .size(size)
                .storageKey(storageKey)
                .build();
        if (reusesStoragePath) {
            imageRepository.delete(previousImage);
            imageRepository.flush();
        }
        imageRepository.save(image);
        if (!reusesStoragePath) {
            removePrevious(previousImage);
        }
    }

    private void removePrevious(ImageEntity image) {
        if (image == null) return;
        deleteFile(image);
        imageRepository.delete(image);
    }

    private Resource loadResource(ImageEntity image) {
        try {
            Resource resource = imageStorage.load(image.getStorageKey());
            if (resource == null) {
                throw new ApiException(ExceptionResponse.ImageNotFound);
            }
            return resource;
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(ExceptionResponse.ImageNotFound);
        }
    }

    private ResponseEntity<Resource> response(ImageEntity image, Resource resource) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, image.getContentType())
                .body(resource);
    }

    private void deleteFile(ImageEntity image) {
        imageStorage.delete(image.getStorageKey());
    }

    private void validateOwner(String ownerType, String ownerId, String variant) {
        if (invalidSegment(ownerType) || invalidSegment(ownerId) || invalidSegment(variant)) {
            throw new ApiException(ExceptionResponse.InvalidInput);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > maxSizeBytes) {
            throw new ApiException(ExceptionResponse.InvalidInput);
        }
        String contentType = file.getContentType();
        if (!MediaType.IMAGE_JPEG_VALUE.equals(contentType)
                && !MediaType.IMAGE_PNG_VALUE.equals(contentType)
                && !IMAGE_WEBP.equals(contentType)) {
            throw new ApiException(ExceptionResponse.InvalidInput);
        }
    }

    private void validateSize(long size) {
        if (size > maxSizeBytes) {
            throw new ApiException(ExceptionResponse.InvalidInput);
        }
    }

    private String extensionFor(String contentType, String filename) {
        if (MediaType.IMAGE_PNG_VALUE.equals(contentType) || hasExtension(filename, ".png")) {
            return ".png";
        }
        if (IMAGE_WEBP.equals(contentType) || hasExtension(filename, ".webp")) {
            return ".webp";
        }
        return ".jpg";
    }

    private String contentTypeFor(String extension) {
        return switch (extension) {
            case ".png" -> MediaType.IMAGE_PNG_VALUE;
            case ".webp" -> IMAGE_WEBP;
            default -> MediaType.IMAGE_JPEG_VALUE;
        };
    }

    private String resolvePath(String ownerType, String ownerId, String variant, String imageId, String extension) {
        String filename = semanticFilename(ownerType, variant, imageId);
        return ownerType.toLowerCase(Locale.ROOT) + "/" + ownerId + "/" + filename + extension;
    }

    private String semanticFilename(String ownerType, String variant, String imageId) {
        if ("user".equalsIgnoreCase(ownerType) && "profile".equalsIgnoreCase(variant)) {
            return "avatar";
        }
        if ("game".equalsIgnoreCase(ownerType) && "cover".equalsIgnoreCase(variant)) {
            return "cover";
        }
        if ("achievement".equalsIgnoreCase(ownerType) && "icon".equalsIgnoreCase(variant)) {
            return "icon";
        }
        if ("guide".equalsIgnoreCase(ownerType)) {
            return imageId;
        }
        return variant.toLowerCase(Locale.ROOT);
    }

    private boolean hasExtension(String filename, String extension) {
        return filename != null && filename.toLowerCase(Locale.ROOT).endsWith(extension);
    }

    private boolean invalidSegment(String value) {
        return value == null || !value.matches("[a-zA-Z0-9_-]+");
    }

}
