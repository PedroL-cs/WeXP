package com.wexp.feature.image;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class ImagePersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(ImagePersistenceService.class);

    private final ImageRepository imageRepository;
    private final ImageStorage imageStorage;

    @Transactional
    public ImageEntity replace(ImageEntity incoming) {
        // Register rollback cleanup before accessing the database, including failures on flush/commit.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) deleteFile(incoming.getStorageKey());
            }
        });

        ImageEntity existing = imageRepository.findByOwnerTypeAndOwnerIdAndVariant(
                incoming.getOwnerType(), incoming.getOwnerId(), incoming.getVariant()).orElse(null);
        if (existing == null) return imageRepository.saveAndFlush(incoming);

        String previousKey = existing.getStorageKey();
        existing.setOriginalFilename(incoming.getOriginalFilename());
        existing.setExtension(incoming.getExtension());
        existing.setContentType(incoming.getContentType());
        existing.setSize(incoming.getSize());
        existing.setStorageKey(incoming.getStorageKey());
        deleteAfterCommit(previousKey);
        return imageRepository.saveAndFlush(existing);
    }

    @Transactional
    public void delete(ImageEntity image) {
        imageRepository.delete(image);
        imageRepository.flush();
        deleteAfterCommit(image.getStorageKey());
    }

    private void deleteAfterCommit(String storageKey) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteFile(storageKey);
            }
        });
    }

    private void deleteFile(String storageKey) {
        try {
            imageStorage.delete(storageKey);
        } catch (RuntimeException exception) {
            // A cleanup failure must not turn a committed update into an apparent rollback.
            logger.warn("Não foi possível limpar o arquivo de imagem {}", storageKey, exception);
        }
    }
}
