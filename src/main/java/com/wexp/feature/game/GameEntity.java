package com.wexp.feature.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wexp.feature.achievement.AchievementEntity;
import com.wexp.feature.category.CategoryEntity;
import com.wexp.feature.genre.GenreEntity;
import com.wexp.shared.util.PublicIdGenerator;
import com.wexp.shared.util.PublicIdType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

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
    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 16)
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

    @Column(name = "release_date")
    private LocalDateTime releaseDate;

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "game_categories",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<CategoryEntity> categories = new HashSet<>();

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "game_genres",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private Set<GenreEntity> genres = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AchievementEntity> achievements = new ArrayList<>();

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

    @JsonProperty("categories")
    public Map<String, Object> getCategoriesResponse() {
        return wrapCollection(this.categories);
    }

    @JsonProperty("genres")
    public Map<String, Object> getGenresResponse() {
        return wrapCollection(this.genres);
    }

    @JsonProperty("achievements")
    public Map<String, Object> getAchievementsResponse() {
        return wrapCollection(this.achievements);
    }

    private <T extends Collection<?>> Map<String, Object> wrapCollection(T collection) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("total", collection != null ? collection.size() : 0);
        map.put("items", collection != null ? collection : Collections.emptyList());
        return map;
    }

    public void addAchievement(AchievementEntity achievement) {
        if (this.achievements == null) this.achievements = new ArrayList<>();
        this.achievements.add(achievement);
        achievement.setGame(this);
    }
}