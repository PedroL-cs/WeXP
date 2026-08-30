package com.wexp.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wexp.utils.PublicIdGenerator;
import com.wexp.utils.PublicIdType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "games")
public class GameEntity {
    @JsonIgnore
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("id")
    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 13)
    private String publicId;

    @Column(name = "steam_app_id", unique = true)
    private Long steamAppId;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(name = "detailed_description", columnDefinition = "TEXT")
    private String detailedDescription;

    @Column(name = "views_count", nullable = false)
    @Builder.Default
    private Long viewsCount = 0L;

    @JsonIgnore
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImageEntity> images = new ArrayList<>();

    @Column(name = "release_date")
    private LocalDateTime releaseDate;

    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "game_categories",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<CategoryEntity> categories = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "game_genres",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private Set<GenreEntity> genres = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (publicId == null) this.publicId = PublicIdGenerator.generate(PublicIdType.GAME);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addImage(ImageEntity image) {
        images.add(image);
        image.setGame(this);
    }

    @JsonProperty("images")
    public Map<String, String> getImageMap() {
        if (this.images == null || this.images.isEmpty()) return new HashMap<>();

        String baseUrl;
        try { baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString(); }
        catch (Exception e) { baseUrl = "http://localhost:8080"; }
        final String finalBaseUrl = baseUrl;

        return this.images.stream().collect(Collectors.toMap(
                img -> img.getType().name().toLowerCase(),
                img -> finalBaseUrl + "/api/v1/images/" + this.publicId + "/" + img.getType().name().toLowerCase()
        ));
    }
}
