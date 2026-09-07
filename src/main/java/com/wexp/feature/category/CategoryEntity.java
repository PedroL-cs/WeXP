package com.wexp.feature.category;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wexp.feature.game.GameEntity;
import com.wexp.shared.util.PublicIdGenerator;
import com.wexp.shared.util.PublicIdType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class CategoryEntity {

    @JsonIgnore
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("id")
    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 16)
    private String publicId;

    @JsonIgnore
    @Column(name = "steam_category_id", unique = true, nullable = false)
    private Long steamCategoryId;

    @Column(nullable = false)
    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "categories")
    @Builder.Default
    private Set<GameEntity> games = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (publicId == null) this.publicId = PublicIdGenerator.generate(PublicIdType.CATEGORY);
    }
}
