package com.graduation.project.auth.entity;

import com.graduation.project.common.entity.User;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "user_relations", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "follower_id", "following_id" })
}, indexes = {
        @Index(name = "idx_following_follower", columnList = "following_id, follower_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRelation {

    @EmbeddedId
    private UserRelationId id;

    @MapsId("followerId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @MapsId("followingId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class UserRelationId implements Serializable {
        @Column(name = "follower_id")
        private UUID followerId;

        @Column(name = "following_id")
        private UUID followingId;
    }
}
