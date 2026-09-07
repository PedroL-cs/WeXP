package com.wexp.feature.revision;

import com.wexp.feature.guide.GuideEntity;
import com.wexp.feature.user.UserEntity;
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
@Table(name = "guide_revisions")
public class GuideRevisionEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 16)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id", nullable = false)
    private GuideEntity guide;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String changeSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RevisionStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.publicId == null) this.publicId = PublicIdGenerator.generate(PublicIdType.REVISION);
        this.createdAt = LocalDateTime.now();
        this.status = RevisionStatus.PENDING;
    }

}
