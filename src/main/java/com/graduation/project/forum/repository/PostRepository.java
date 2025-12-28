package com.graduation.project.forum.repository;

import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.entity.Post;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository
                extends JpaRepository<Post, UUID>, JpaSpecificationExecutor<Post> {

        @EntityGraph(attributePaths = { "author", "topic" })
        Page<Post> findByTopicIdAndPostStatusAndDeletedFalse(
                        UUID topicId, PostStatus postStatus, Pageable pageable);

        @Override
        @EntityGraph(attributePaths = { "author", "topic" })
        Page<Post> findAll(Specification<Post> spec, Pageable pageable);

        @EntityGraph(attributePaths = { "topic" })
        Page<Post> findAllByAuthor_Id(UUID userId, Pageable pageable);

        @EntityGraph(attributePaths = { "topic" })
        Page<Post> findAllByAuthor_IdAndPostStatusInAndDeletedFalse(UUID userId, List<PostStatus> statuses,
                        Pageable pageable);

        @Modifying(clearAutomatically = true)
        @Query("UPDATE Post p SET p.reactionCount = p.reactionCount + 1 WHERE p.id = :id")
        void increaseReactionCount(@Param("id") UUID id);

        @Modifying(clearAutomatically = true)
        @Query("UPDATE Post p SET p.reactionCount = p.reactionCount - 1 WHERE p.id = :id AND p.reactionCount > 0")
        int decreaseReactionCount(@Param("id") UUID id);

        @Query("SELECT p.reactionCount FROM Post p WHERE p.id = :id")
        Long getReactionCount(@Param("id") UUID id);

        @Modifying(clearAutomatically = true)
        @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :id")
        void increaseCommentCount(@Param("id") UUID id);

        @Modifying
        @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :id AND p.commentCount > 0")
        int decreaseCommentCount(@Param("id") UUID id);

        boolean existsBySlug(String slug);
}
