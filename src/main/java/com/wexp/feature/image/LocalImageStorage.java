package com.wexp.feature.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class LocalImageStorage implements ImageStorage {

    private static final Logger logger = LoggerFactory.getLogger(LocalImageStorage.class);
    private final Path root;

    public LocalImageStorage(@Value("${images.storage.location:uploads/images}") String location) {
        this.root = Paths.get(location).toAbsolutePath().normalize();
    }

    @Override
    public void store(String storageKey, InputStream input) throws Exception {
        Path target = resolve(storageKey);
        Files.createDirectories(target.getParent());
        Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public Resource load(String storageKey) {
        try {
            Resource resource = new UrlResource(resolve(storageKey).toUri());
            return resource.exists() && resource.isReadable() ? resource : null;
        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (Exception exception) {
            logger.warn("Não foi possível excluir o arquivo de imagem {}", storageKey, exception);
        }
    }

    @Override
    public long size(String storageKey) throws Exception {
        return Files.size(resolve(storageKey));
    }

    private Path resolve(String storageKey) {
        Path resolved = root.resolve(storageKey).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Caminho de imagem inválido");
        }
        return resolved;
    }
}
