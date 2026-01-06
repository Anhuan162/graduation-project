package com.graduation.project.forum.repository;

import com.graduation.project.forum.dto.CommentResponse;
import com.graduation.project.forum.dto.CommentWithReplyCountResponse;
import com.graduation.project.forum.entity.Comment;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
        @Query("SELECT new com.graduation.project.forum.dto.CommentWithReplyCountResponse("
                        + "c.id, c.content, c.author.id, c.author.fullName, c.author.avatarUrl, c.createdDateTime, COUNT(r), f.url, c.parent.id) "
                        + "FROM Comment c "
                        + "LEFT JOIN c.replies r "
                        + "LEFT JOIN FileMetadata f ON f.resourceId = c.id AND f.resourceType = 'COMMENT' "
                        + "WHERE c.post.id = :postId AND c.deleted IS FALSE "
                        + "GROUP BY c.id, c.content, c.author.id, c.author.fullName, c.author.avatarUrl, c.createdDateTime, f.url, c.parent.id")
        Page<CommentWithReplyCountResponse> findRootCommentsWithCount(
                        @Param("postId") UUID postId, Pageable pageable);

        @Query("SELECT c FROM Comment c "
                        + "LEFT JOIN FETCH c.author "
                        + // Fetch author để tránh lazy loading
                        "WHERE c.parent.id = :parentId")
        Page<Comment> findRepliesByParentId(@Param("parentId") UUID parentId, Pageable pageable);

        @Modifying(clearAutomatically = true)
        @Query("UPDATE Comment c SET c.reactionCount = COALESCE(c.reactionCount, 0) + 1 WHERE c.id = :commentId")
        void increaseReactionCount(@Param("commentId") UUID commentId);

        @Modifying(clearAutomatically = true)
        @Query("UPDATE Comment c SET c.reactionCount = CASE WHEN COALESCE(c.reactionCount, 0) > 0 THEN c.reactionCount - 1 ELSE 0 END WHERE c.id = :commentId")
        void decreaseReactionCount(@Param("commentId") UUID commentId);

        Page<CommentResponse> findAll(Specification<Comment> spec, Pageable pageable);

        Page<Comment> findAllByAuthorIdAndDeletedFalse(UUID id, Pageable pageable);
}
