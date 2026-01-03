package com.graduation.project.forum.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "post_view_logs", uniqueConstraints = {
                @UniqueConstraint(name = "ux_post_view_logs_post_viewer", columnNames = { "post_id", "viewer_key" })
}, indexes = {
                @Index(name = "ix_post_view_logs_post", columnList = "post_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostViewLog {

        @PrePersist
        public void generateId() {
                if (this.id == null) {
                        this.id = UUID.randomUUID();
                }
        }

        @Id
        @Column(nullable = false, updatable = false)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "post_id", nullable = false)
        private Post post;

        @Column(name = "viewer_key", nullable = false, length = 128)
        private String viewerKey;

        @Column(name = "first_viewed_at", nullable = false)
        private Instant firstViewedAt;

        @Column(name = "last_viewed_at", nullable = false)
        private Instant lastViewedAt;

        @Column(name = "view_count", nullable = false)
        @Builder.Default
        private long viewCount = 0L;
}
