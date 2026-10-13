package com.wexp;

import com.wexp.feature.achievement.AchievementEntity;
import com.wexp.feature.achievement.IAchievementRepository;
import com.wexp.feature.auth.AuthService;
import com.wexp.feature.auth.JwtService;
import com.wexp.feature.auth.dto.RegisterRequestDto;
import com.wexp.feature.game.GameEntity;
import com.wexp.feature.game.IGameRepository;
import com.wexp.feature.guide.GuideService;
import com.wexp.feature.revision.GuideRevisionLifecycleService;
import com.wexp.feature.guide.IGuideRepository;
import com.wexp.feature.guide.dto.CreateGuideRequestDto;
import com.wexp.feature.image.*;
import com.wexp.feature.user.IUserRepository;
import com.wexp.feature.user.UserEntity;
import com.wexp.feature.user.UserService;
import com.wexp.feature.user.dto.UpdateUserRequestDto;
import com.wexp.shared.exception.ApiException;
import com.wexp.shared.exception.ExceptionResponse;
import com.wexp.shared.persistence.PersistenceTransactions;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"}, showSql = false)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({ImageService.class, ImagePersistenceService.class, LocalImageStorage.class,
        PersistenceTransactions.class, AuthService.class, GuideService.class, UserService.class, GuideRevisionLifecycleService.class})
class PersistenceIntegrityTests {

    @TempDir
    static Path storageRoot;

    @DynamicPropertySource
    static void storageProperties(DynamicPropertyRegistry registry) {
        registry.add("images.storage.location", () -> storageRoot.toString());
    }

    @Autowired private ImageService imageService;
    @Autowired private ImagePersistenceService imagePersistenceService;
    @Autowired private ImageRepository imageRepository;
    @Autowired private ImageStorage imageStorage;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private PersistenceTransactions persistenceTransactions;
    @Autowired private AuthService authService;
    @Autowired private GuideService guideService;
    @Autowired private UserService userService;
    @Autowired private IUserRepository userRepository;
    @Autowired private IGuideRepository guideRepository;
    @Autowired private IGameRepository gameRepository;
    @Autowired private IAchievementRepository achievementRepository;
    @PersistenceContext private EntityManager entityManager;

    @MockitoBean private ImageAuthorizationService imageAuthorizationService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private PasswordEncoder passwordEncoder;
    @MockitoBean private AuthenticationManager authenticationManager;
    @MockitoSpyBean private LocalImageStorage localImageStorage;

    private String ownerId;

    @BeforeEach
    void setup() {
        ownerId = UUID.randomUUID().toString();
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(jwtService.generateToken(any(UserEntity.class))).thenReturn("token");
    }

    @Test
    void shouldReplaceImageWithoutOverwritingPreviousFile() throws Exception {
        ImageEntity original = upload("old.jpg", "image/jpeg", new byte[]{1, 2});
        ImageEntity replacement = upload("new.png", "image/png", new byte[]{3, 4});

        assertEquals(original.getPublicId(), replacement.getPublicId());
        assertNotEquals(original.getStorageKey(), replacement.getStorageKey());
        assertTrue(replacement.getVersion() > original.getVersion());
        assertFalse(Files.exists(storageRoot.resolve(original.getStorageKey())));
        assertArrayEquals(new byte[]{3, 4}, Files.readAllBytes(storageRoot.resolve(replacement.getStorageKey())));
        assertEquals(replacement.getStorageKey(), currentImage().getStorageKey());
        assertEquals(1, imageRepository.findAll().stream().filter(i -> ownerId.equals(i.getOwnerId())).count());
    }

    @Test
    void shouldKeepPreviousFileUntilCommit() {
        ImageEntity original = upload("old.jpg", "image/jpeg", new byte[]{1});
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        ImageEntity replacement = transaction.execute(status -> {
            ImageEntity image = upload("new.jpg", "image/jpeg", new byte[]{2});
            assertTrue(Files.exists(storageRoot.resolve(original.getStorageKey())));
            assertTrue(Files.exists(storageRoot.resolve(image.getStorageKey())));
            return image;
        });

        assertNotNull(replacement);
        assertFalse(Files.exists(storageRoot.resolve(original.getStorageKey())));
        assertTrue(Files.exists(storageRoot.resolve(replacement.getStorageKey())));
    }

    @Test
    void shouldRestorePreviousMetadataAndRemoveNewFileOnOuterRollback() throws Exception {
        ImageEntity original = upload("old.jpg", "image/jpeg", new byte[]{1});
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        ImageEntity replacement = transaction.execute(status -> {
            ImageEntity image = upload("new.jpg", "image/jpeg", new byte[]{2});
            status.setRollbackOnly();
            return image;
        });

        assertNotNull(replacement);
        assertEquals(original.getStorageKey(), currentImage().getStorageKey());
        assertArrayEquals(new byte[]{1}, Files.readAllBytes(storageRoot.resolve(original.getStorageKey())));
        assertFalse(Files.exists(storageRoot.resolve(replacement.getStorageKey())));
    }

    @Test
    void shouldRemoveFirstUploadOnRollback() {
        ImageEntity image = new TransactionTemplate(transactionManager).execute(status -> {
            ImageEntity created = upload("first.jpg", "image/jpeg", new byte[]{1});
            status.setRollbackOnly();
            return created;
        });

        assertNotNull(image);
        assertTrue(imageRepository.findByOwnerTypeAndOwnerIdAndVariant("user", ownerId, "profile").isEmpty());
        assertFalse(Files.exists(storageRoot.resolve(image.getStorageKey())));
    }

    @Test
    void shouldPreserveImageWhenMetadataWriteFails() throws Exception {
        ImageEntity original = upload("old.jpg", "image/jpeg", new byte[]{1});
        List<Path> before;
        try (var files = Files.list(storageRoot.resolve("user/" + ownerId))) {
            before = files.sorted().toList();
        }

        ApiException failure = assertThrows(ApiException.class,
                () -> upload("x".repeat(300) + ".jpg", "image/jpeg", new byte[]{2}));
        assertEquals(ExceptionResponse.InternalServerError, failure.getExceptionResponse());

        assertEquals(original.getStorageKey(), currentImage().getStorageKey());
        assertArrayEquals(new byte[]{1}, Files.readAllBytes(storageRoot.resolve(original.getStorageKey())));
        try (var files = Files.list(storageRoot.resolve("user/" + ownerId))) {
            assertEquals(before, files.sorted().toList());
        }
    }

    @Test
    void shouldPreserveFileAndRowWhenDeletionRollsBack() {
        ImageEntity original = upload("old.jpg", "image/jpeg", new byte[]{1});

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            imagePersistenceService.delete(original);
            assertTrue(Files.exists(storageRoot.resolve(original.getStorageKey())));
            status.setRollbackOnly();
        });

        assertEquals(original.getPublicId(), currentImage().getPublicId());
        assertTrue(Files.exists(storageRoot.resolve(original.getStorageKey())));
    }

    @Test
    void shouldDeleteFileOnlyAfterDeletionCommits() {
        ImageEntity original = upload("old.jpg", "image/jpeg", new byte[]{1});
        imagePersistenceService.delete(original);

        assertTrue(imageRepository.findByPublicId(original.getPublicId()).isEmpty());
        assertFalse(Files.exists(storageRoot.resolve(original.getStorageKey())));
    }

    @Test
    void shouldRejectStaleDeletionWithoutDeletingCurrentFile() {
        ImageEntity stale = upload("old.jpg", "image/jpeg", new byte[]{1});
        ImageEntity replacement = upload("new.jpg", "image/jpeg", new byte[]{2});

        assertThrows(OptimisticLockingFailureException.class, () -> imagePersistenceService.delete(stale));

        assertEquals(replacement.getStorageKey(), currentImage().getStorageKey());
        assertTrue(Files.exists(storageRoot.resolve(replacement.getStorageKey())));
    }

    @Test
    void shouldRejectStaleMetadataUpdate() {
        ImageEntity stale = upload("old.jpg", "image/jpeg", new byte[]{1});
        ImageEntity replacement = upload("new.jpg", "image/jpeg", new byte[]{2});
        stale.setOriginalFilename("stale.jpg");

        assertThrows(OptimisticLockingFailureException.class,
                () -> persistenceTransactions.insert(() -> imageRepository.saveAndFlush(stale)));
        assertEquals(replacement.getStorageKey(), currentImage().getStorageKey());
    }

    @Test
    void shouldKeepCommittedReplacementWhenOldFileCleanupFails() {
        ImageEntity original = upload("old.jpg", "image/jpeg", new byte[]{1});
        doThrow(new IllegalStateException("filesystem unavailable")).when(localImageStorage).delete(original.getStorageKey());

        ImageEntity replacement = upload("new.jpg", "image/jpeg", new byte[]{2});

        assertEquals(replacement.getStorageKey(), currentImage().getStorageKey());
        assertTrue(Files.exists(storageRoot.resolve(replacement.getStorageKey())));
        assertTrue(Files.exists(storageRoot.resolve(original.getStorageKey())));
    }

    @Test
    void shouldRollbackAvatarWhenProfileCommitFails() throws Exception {
        UserEntity user = userRepository.saveAndFlush(UserEntity.builder()
                .login("u" + ownerId.substring(0, 20)).email(ownerId + "@example.com").username("Original")
                .passwordHash("encoded").birthDate(LocalDate.of(1990, 1, 1)).build());
        ownerId = user.getPublicId();
        ImageEntity original = upload("old.jpg", "image/jpeg", new byte[]{1});
        UpdateUserRequestDto update = new UpdateUserRequestDto();
        update.setUsername("x".repeat(101));

        assertThrows(DataIntegrityViolationException.class, () -> userService.updateCurrentUser(user, update,
                new MockMultipartFile("avatar", "new.jpg", "image/jpeg", new byte[]{2})));

        assertEquals("Original", entityManager.createQuery(
                        "select u.username from UserEntity u where u.publicId = :id", String.class)
                .setParameter("id", ownerId).getSingleResult());
        assertEquals(original.getStorageKey(), currentImage().getStorageKey());
        assertArrayEquals(new byte[]{1}, Files.readAllBytes(storageRoot.resolve(original.getStorageKey())));
        try (var files = Files.list(storageRoot.resolve("user/" + ownerId))) {
            assertEquals(List.of(storageRoot.resolve(original.getStorageKey())), files.toList());
        }
    }

    @Test
    void shouldEnforceOwnerVariantUniquenessForConcurrentInserts() throws Exception {
        List<Object> outcomes = concurrently(() -> {
            ImageEntity image = ImageEntity.builder().ownerType("user").ownerId(ownerId).variant("profile")
                    .extension(".jpg").contentType("image/jpeg").size(1L)
                    .storageKey(UUID.randomUUID().toString()).build();
            return persistenceTransactions.insert(() -> imageRepository.saveAndFlush(image));
        });

        assertEquals(1, outcomes.stream().filter(ImageEntity.class::isInstance).count());
        assertEquals(1, outcomes.stream().filter(DataIntegrityViolationException.class::isInstance).count());
        assertNotNull(currentImage());
    }

    @Test
    void shouldReturnDomainConflictForConcurrentRegistrations() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setLogin("u" + ownerId.substring(0, 20));
        request.setEmail(ownerId + "@example.com");
        request.setUsername("Test User");
        request.setPassword("password123");
        request.setBirthDate(LocalDate.of(1990, 1, 1));

        List<Object> outcomes = concurrently(() -> authService.register(request));

        assertEquals(1, outcomes.stream().filter(ApiException.class::isInstance).count());
        ApiException conflict = (ApiException) outcomes.stream().filter(ApiException.class::isInstance).findFirst().orElseThrow();
        assertEquals(ExceptionResponse.EmailAlreadyInUse, conflict.getExceptionResponse());
        assertEquals(1, outcomes.stream().filter(o -> !(o instanceof Exception)).count());
        assertTrue(userRepository.findByEmail(request.getEmail()).isPresent());
    }

    @Test
    void shouldReturnDomainConflictForConcurrentGuideCreation() throws Exception {
        UserEntity author = userRepository.saveAndFlush(UserEntity.builder()
                .login("u" + ownerId.substring(0, 20)).email(ownerId + "@example.com").username("Author")
                .passwordHash("encoded").birthDate(LocalDate.of(1990, 1, 1)).build());
        GameEntity game = gameRepository.saveAndFlush(GameEntity.builder().name("Game").build());
        AchievementEntity achievement = achievementRepository.saveAndFlush(AchievementEntity.builder()
                .name("Achievement").isHidden(false).game(game).build());

        List<Object> outcomes = concurrently(() -> guideService.createInitialGuide(achievement.getPublicId(),
                new CreateGuideRequestDto("Conteúdo suficientemente longo"), author));

        assertEquals(1, outcomes.stream().filter(ApiException.class::isInstance).count());
        ApiException conflict = (ApiException) outcomes.stream().filter(ApiException.class::isInstance).findFirst().orElseThrow();
        assertEquals(ExceptionResponse.GuideAlreadyExists, conflict.getExceptionResponse());
        assertEquals(1, outcomes.stream().filter(o -> !(o instanceof Exception)).count());
        assertTrue(guideRepository.findByAchievement_PublicId(achievement.getPublicId()).isPresent());
    }

    private ImageEntity upload(String name, String contentType, byte[] bytes) {
        return imageService.replace("user", ownerId, "profile",
                new MockMultipartFile("file", name, contentType, bytes), null);
    }

    private ImageEntity currentImage() {
        return imageRepository.findByOwnerTypeAndOwnerIdAndVariant("user", ownerId, "profile").orElseThrow();
    }

    private List<Object> concurrently(Callable<?> operation) throws Exception {
        var executor = Executors.newFixedThreadPool(2);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        Callable<Object> task = () -> {
            ready.countDown();
            if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Timed out waiting for start");
            try {
                return operation.call();
            } catch (RuntimeException exception) {
                return exception;
            }
        };
        try {
            var first = executor.submit(task);
            var second = executor.submit(task);
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            return List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}
