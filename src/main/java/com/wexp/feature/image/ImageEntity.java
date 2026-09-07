package com.wexp.feature.image;

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
@Table(name = "images", indexes = {
        @Index(name = "idx_image_owner", columnList = "owner_type, owner_id, variant")
})
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 16)
    private String publicId;

    @Column(name = "owner_type", nullable = false, length = 50)
    private String ownerType;

    @Column(name = "owner_id", nullable = false, length = 100)
    private String ownerId;

    @Column(nullable = false, length = 100)
    private String variant;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "extension", nullable = false, length = 10)
    private String extension;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Column(name = "storage_key", nullable = false, unique = true)
    private String storageKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (publicId == null) publicId = PublicIdGenerator.generate(PublicIdType.IMAGE);
        createdAt = LocalDateTime.now();
    }
}
