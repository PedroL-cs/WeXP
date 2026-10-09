package com.wexp;

import com.wexp.feature.auth.JwtService;
import com.wexp.feature.guide.GuideEntity;
import com.wexp.feature.guide.IGuideRepository;
import com.wexp.feature.image.ImageAuthorizationService;
import com.wexp.feature.image.ImageController;
import com.wexp.feature.image.ImageEntity;
import com.wexp.feature.image.ImageRepository;
import com.wexp.feature.image.ImageService;
import com.wexp.feature.image.ImagePersistenceService;
import com.wexp.feature.image.ImageStorage;
import com.wexp.feature.user.UserEntity;
import com.wexp.shared.config.security.CustomAccessDeniedHandler;
import com.wexp.shared.config.security.CustomAuthenticationEntryPoint;
import com.wexp.shared.config.security.JwtAuthenticationFilter;
import com.wexp.shared.config.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImageController.class)
@Import({ImageService.class, ImageAuthorizationService.class, SecurityConfig.class,
        JwtAuthenticationFilter.class, CustomAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class})
class ImageAuthorizationApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @MockitoBean
    private ImageRepository imageRepository;
    @MockitoBean
    private ImageStorage imageStorage;
    @MockitoBean
    private ImagePersistenceService imagePersistenceService;
    @MockitoBean
    private IGuideRepository guideRepository;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private UserEntity currentUser;

    @BeforeEach
    void authenticateUser() {
        // Register the security chain only: the JWT filter already belongs to it.
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        currentUser = UserEntity.builder().publicId("usr-1").login("usuario").build();
        when(jwtService.extractUsername("token")).thenReturn("usuario");
        when(jwtService.isTokenValid("token", currentUser)).thenReturn(true);
        when(userDetailsService.loadUserByUsername("usuario")).thenReturn(currentUser);
    }

    @Test
    void shouldRequireAuthenticationForImageChanges() throws Exception {
        mockMvc.perform(upload("user", "usr-1", "profile"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/v1/images/img-1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(imageRepository, imageStorage, guideRepository);
    }

    @Test
    void shouldAllowUploadingOwnAvatar() throws Exception {
        when(imagePersistenceService.replace(any(ImageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(upload("user", "usr-1", "profile").header("Authorization", "Bearer token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value("usr-1"));

        verify(imageStorage).store(anyString(), any());
        verify(imagePersistenceService).replace(any(ImageEntity.class));
    }

    @ParameterizedTest
    @CsvSource({"user,usr-2,profile", "user,usr-1,cover", "game,game-1,cover",
            "achievement,ach-1,icon", "unknown,usr-1,profile", "USER,usr-1,profile"})
    void shouldRejectUnauthorizedUploads(String ownerType, String ownerId, String variant) throws Exception {
        mockMvc.perform(upload(ownerType, ownerId, variant).header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(16));

        verifyNoInteractions(imageRepository, imageStorage, imagePersistenceService);
    }

    @Test
    void shouldAllowGuideAuthorToUploadImage() throws Exception {
        when(guideRepository.findByPublicId("guide-1"))
                .thenReturn(Optional.of(GuideEntity.builder().author(currentUser).build()));
        when(imagePersistenceService.replace(any(ImageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(upload("guide", "guide-1", "illustration").header("Authorization", "Bearer token"))
                .andExpect(status().isCreated());

        verify(imageStorage).store(anyString(), any());
    }

    @Test
    void shouldRejectUploadingToAnotherAuthorsGuide() throws Exception {
        when(guideRepository.findByPublicId("guide-1"))
                .thenReturn(Optional.of(GuideEntity.builder()
                        .author(UserEntity.builder().publicId("usr-2").build()).build()));

        mockMvc.perform(upload("guide", "guide-1", "illustration").header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(imageRepository, imageStorage, imagePersistenceService);
    }

    @Test
    void shouldRejectUploadingToMissingGuide() throws Exception {
        mockMvc.perform(upload("guide", "missing", "illustration").header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(imageRepository, imageStorage, imagePersistenceService);
    }

    @ParameterizedTest
    @CsvSource({"user,usr-2,profile", "user,usr-1,cover", "game,game-1,cover",
            "achievement,ach-1,icon", "unknown,usr-1,profile"})
    void shouldRejectUnauthorizedDeletion(String ownerType, String ownerId, String variant) throws Exception {
        ImageEntity image = image(ownerType, ownerId, variant);
        when(imageRepository.findByPublicId("img-1")).thenReturn(Optional.of(image));

        mockMvc.perform(delete("/api/v1/images/img-1").header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(imageStorage);
        verifyNoInteractions(imagePersistenceService);
    }

    @Test
    void shouldRejectDeletingAnotherAuthorsGuideImage() throws Exception {
        when(imageRepository.findByPublicId("img-1"))
                .thenReturn(Optional.of(image("guide", "guide-1", "illustration")));
        when(guideRepository.findByPublicId("guide-1"))
                .thenReturn(Optional.of(GuideEntity.builder()
                        .author(UserEntity.builder().publicId("usr-2").build()).build()));

        mockMvc.perform(delete("/api/v1/images/img-1").header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(imageStorage);
        verifyNoInteractions(imagePersistenceService);
    }

    @ParameterizedTest
    @CsvSource({"user,usr-1,profile", "guide,guide-1,illustration"})
    void shouldAllowOwnerToDeleteImage(String ownerType, String ownerId, String variant) throws Exception {
        ImageEntity image = image(ownerType, ownerId, variant);
        when(imageRepository.findByPublicId("img-1")).thenReturn(Optional.of(image));
        if ("guide".equals(ownerType)) {
            when(guideRepository.findByPublicId(ownerId))
                    .thenReturn(Optional.of(GuideEntity.builder().author(currentUser).build()));
        }

        mockMvc.perform(delete("/api/v1/images/img-1").header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());

        verify(imagePersistenceService).delete(image);
    }

    @Test
    void shouldKeepImageReadingPublic() throws Exception {
        ImageEntity image = image("user", "usr-2", "profile");
        when(imageRepository.findByPublicId("img-1")).thenReturn(Optional.of(image));
        when(imageStorage.load("image.jpg")).thenReturn(new ByteArrayResource(new byte[]{1, 2, 3}));

        mockMvc.perform(get("/api/v1/images/img-1"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void shouldReturnConflictAndCleanNewFileOnConcurrentReplacement() throws Exception {
        when(imagePersistenceService.replace(any(ImageEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("stale image"));

        mockMvc.perform(upload("user", "usr-1", "profile").header("Authorization", "Bearer token"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(18));

        verify(imageStorage).delete(anyString());
    }

    @Test
    void shouldReturnConflictOnConcurrentDeletion() throws Exception {
        ImageEntity image = image("user", "usr-1", "profile");
        when(imageRepository.findByPublicId("img-1")).thenReturn(Optional.of(image));
        doThrow(new OptimisticLockingFailureException("stale image")).when(imagePersistenceService).delete(image);

        mockMvc.perform(delete("/api/v1/images/img-1").header("Authorization", "Bearer token"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(18));

        verifyNoInteractions(imageStorage);
    }

    private MockMultipartHttpServletRequestBuilder upload(String ownerType, String ownerId, String variant) {
        return multipart("/api/v1/images")
                .file(new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[]{1, 2, 3}))
                .param("ownerType", ownerType)
                .param("ownerId", ownerId)
                .param("variant", variant);
    }

    private ImageEntity image(String ownerType, String ownerId, String variant) {
        return ImageEntity.builder().publicId("img-1").ownerType(ownerType).ownerId(ownerId)
                .variant(variant).storageKey("image.jpg").contentType("image/jpeg").build();
    }
}
