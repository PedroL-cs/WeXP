package com.wexp.feature.achievement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wexp.feature.game.GameEntity;
import com.wexp.feature.guide.GuideEntity;
import com.wexp.shared.util.PublicIdGenerator;
import com.wexp.shared.util.PublicIdType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "achievements")
public class AchievementEntity {
    @JsonIgnore
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("id")
    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 16)
    private String publicId;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false, updatable = false)
    private GameEntity game;

    @JsonIgnore
    @OneToOne(mappedBy = "achievement", cascade = CascadeType.ALL)
    private GuideEntity guide;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.publicId == null) this.publicId = PublicIdGenerator.generate(PublicIdType.ACHIEVEMENT);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
