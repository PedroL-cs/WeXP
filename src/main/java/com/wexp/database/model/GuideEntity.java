package com.wexp.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wexp.utils.PublicIdGenerator;
import com.wexp.utils.PublicIdType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "guides")
public class GuideEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("id")
    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 16)
    private String publicId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "achivement_id", nullable = false, unique = true)
    private AchievementEntity achievement;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.publicId == null) this.publicId = PublicIdGenerator.generate(PublicIdType.GUIDE);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @JsonProperty("achievementId")
    public String getAchievementPublicId() {
        return this.achievement != null ? this.achievement.getPublicId() : null;
    }

    @JsonProperty("author")
    public String getAuthorUsername() {
        return this.author != null ? this.author.getUsername() : null;
    }
}
