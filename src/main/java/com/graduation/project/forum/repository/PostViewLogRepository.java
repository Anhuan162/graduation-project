package com.graduation.project.forum.repository;

import com.graduation.project.forum.entity.PostViewLog;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostViewLogRepository extends JpaRepository<PostViewLog, UUID> {

    Optional<PostViewLog> findByPost_IdAndViewerKey(UUID postId, String viewerKey);

    @Modifying(clearAutomatically = true)
    @Query("""
                UPDATE PostViewLog v
                SET v.lastViewedAt = :now,
                    v.viewCount = COALESCE(v.viewCount, 0) + 1
                WHERE v.id = :id
            """)
    int touchAndIncrease(@Param("id") UUID id, @Param("now") Instant now);
}
