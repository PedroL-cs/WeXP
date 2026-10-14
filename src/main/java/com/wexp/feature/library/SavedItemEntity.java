package com.wexp.feature.library;

import com.wexp.feature.achievement.AchievementEntity;
import com.wexp.feature.game.GameEntity;
import com.wexp.feature.guide.GuideEntity;
import com.wexp.feature.user.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "saved_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_saved_item_user_game", columnNames = {"user_id", "game_id"}),
        @UniqueConstraint(name = "uk_saved_item_user_achievement", columnNames = {"user_id", "achievement_id"}),
        @UniqueConstraint(name = "uk_saved_item_user_guide", columnNames = {"user_id", "guide_id"})
}, indexes = {
        @Index(name = "idx_saved_item_user_date", columnList = "user_id, saved_at"),
        @Index(name = "idx_saved_item_user_type_date", columnList = "user_id, resource_type, saved_at")
})
public class SavedItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_saved_item_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, updatable = false, length = 20)
    private SavedResourceType resourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", updatable = false,
            foreignKey = @ForeignKey(name = "fk_saved_item_game"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GameEntity game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", updatable = false,
            foreignKey = @ForeignKey(name = "fk_saved_item_achievement"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AchievementEntity achievement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id", updatable = false,
            foreignKey = @ForeignKey(name = "fk_saved_item_guide"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GuideEntity guide;

    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    public static SavedItemEntity game(UserEntity user, GameEntity game) {
        return SavedItemEntity.builder()
                .user(user)
                .resourceType(SavedResourceType.GAME)
                .game(game)
                .build();
    }

    public static SavedItemEntity achievement(UserEntity user, AchievementEntity achievement) {
        return SavedItemEntity.builder()
                .user(user)
                .resourceType(SavedResourceType.ACHIEVEMENT)
                .achievement(achievement)
                .build();
    }

    public static SavedItemEntity guide(UserEntity user, GuideEntity guide) {
        return SavedItemEntity.builder()
                .user(user)
                .resourceType(SavedResourceType.GUIDE)
                .guide(guide)
                .build();
    }

    @PrePersist
    void onCreate() {
        validateResource();
        savedAt = LocalDateTime.now();
    }

    public String getResourcePublicId() {
        return switch (resourceType) {
            case GAME -> game.getPublicId();
            case ACHIEVEMENT -> achievement.getPublicId();
            case GUIDE -> guide.getPublicId();
        };
    }

    private void validateResource() {
        int resourceCount = (game == null ? 0 : 1)
                + (achievement == null ? 0 : 1)
                + (guide == null ? 0 : 1);
        boolean matchesType = resourceType != null && switch (resourceType) {
            case GAME -> game != null;
            case ACHIEVEMENT -> achievement != null;
            case GUIDE -> guide != null;
        };
        if (user == null || resourceCount != 1 || !matchesType) {
            throw new IllegalStateException("Um item salvo deve pertencer a um usuário e referenciar exatamente um recurso");
        }
    }
}
