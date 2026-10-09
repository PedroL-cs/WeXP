package com.wexp;

import com.wexp.feature.image.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoteImageIntegrityTests {

    @TempDir private Path directory;
    @Mock private ImageRepository imageRepository;
    @Mock private ImageAuthorizationService imageAuthorizationService;
    @Mock private ImagePersistenceService imagePersistenceService;

    @Test
    void shouldRemoveRemoteFileAfterMetadataFailureWithoutTouchingPreviousFile() throws Exception {
        Path source = directory.resolve("source.jpg");
        Files.write(source, new byte[]{1, 2});
        LocalImageStorage storage = spy(new LocalImageStorage(directory.resolve("storage").toString()));
        try (var input = Files.newInputStream(source)) {
            storage.store("game/game-1/cover.jpg", input);
        }
        ImageEntity previous = ImageEntity.builder().storageKey("game/game-1/cover.jpg").build();
        when(imageRepository.findByOwnerTypeAndOwnerIdAndVariant("game", "game-1", "cover"))
                .thenReturn(java.util.Optional.of(previous));
        // Trigger the download because the previous file is temporarily unavailable to the reader.
        doReturn(null).when(storage).load(previous.getStorageKey());
        CountDownLatch cleaned = new CountDownLatch(1);
        doAnswer(invocation -> {
            try {
                invocation.callRealMethod();
                return null;
            } finally {
                cleaned.countDown();
            }
        }).when(storage).delete(anyString());
        when(imagePersistenceService.replace(any(ImageEntity.class)))
                .thenThrow(new DataIntegrityViolationException("metadata failure"));
        ImageService service = new ImageService(imageRepository, storage, imageAuthorizationService, imagePersistenceService);
        ReflectionTestUtils.setField(service, "maxSizeBytes", 10485760L);

        service.replaceFromUrlAsync("game", "game-1", "cover", source.toUri().toString());

        assertTrue(cleaned.await(5, TimeUnit.SECONDS));
        ArgumentCaptor<ImageEntity> downloaded = ArgumentCaptor.forClass(ImageEntity.class);
        verify(imagePersistenceService).replace(downloaded.capture());
        assertNotEquals(previous.getStorageKey(), downloaded.getValue().getStorageKey());
        assertFalse(Files.exists(directory.resolve("storage").resolve(downloaded.getValue().getStorageKey())));
        assertArrayEquals(new byte[]{1, 2}, Files.readAllBytes(directory.resolve("storage").resolve(previous.getStorageKey())));
    }
}
