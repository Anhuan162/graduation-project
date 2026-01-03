package com.graduation.project.forum.repository;

import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID>, JpaSpecificationExecutor<Post> {

    @EntityGraph(attributePaths = { "author", "topic" })
    Page<Post> findByTopicIdAndPostStatusAndDeletedFalse(UUID topicId, PostStatus postStatus, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = { "author", "topic" })
    Page<Post> findAll(Specification<Post> spec, Pageable pageable);

    @EntityGraph(attributePaths = { "topic", "author" })
    Page<Post> findAllByAuthor_Id(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = { "topic", "author" })
    Page<Post> findAllByAuthor_IdAndPostStatusInAndDeletedFalse(UUID userId, List<PostStatus> statuses,
            Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("""
                UPDATE Post p
                SET p.reactionCount = COALESCE(p.reactionCount, 0) + :delta
                WHERE p.id = :id
            """)
    int updateReactionCount(@Param("id") UUID id, @Param("delta") long delta);

    @Modifying(clearAutomatically = true)
    @Query("""
                UPDATE Post p
                SET p.reactionCount = COALESCE(p.reactionCount, 0) - 1
                WHERE p.id = :id AND COALESCE(p.reactionCount, 0) > 0
            """)
    int decreaseReactionCount(@Param("id") UUID id);

    @Query("SELECT COALESCE(p.reactionCount, 0) FROM Post p WHERE p.id = :id")
    Long getReactionCount(@Param("id") UUID id);

    @Modifying(clearAutomatically = true)
    @Query("""
                UPDATE Post p
                SET p.commentCount = COALESCE(p.commentCount, 0) + 1
                WHERE p.id = :id
            """)
    int increaseCommentCount(@Param("id") UUID id);

    @Modifying(clearAutomatically = true)
    @Query("""
                UPDATE Post p
                SET p.commentCount = COALESCE(p.commentCount, 0) - 1
                WHERE p.id = :id AND COALESCE(p.commentCount, 0) > 0
            """)
    int decreaseCommentCount(@Param("id") UUID id);

    boolean existsBySlug(String slug);

    @Modifying(clearAutomatically = true)
    @Query("""
                UPDATE Post p
                SET p.viewCount = COALESCE(p.viewCount, 0) + :delta
                WHERE p.id = :id
            """)
    int updateViewCount(@Param("id") UUID id, @Param("delta") long delta);

    @Query("SELECT COALESCE(p.viewCount, 0) FROM Post p WHERE p.id = :id")
    Long getViewCount(@Param("id") UUID id);
}
