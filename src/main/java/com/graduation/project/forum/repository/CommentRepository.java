package com.graduation.project.forum.repository;

import com.graduation.project.forum.entity.Comment;
import com.graduation.project.forum.dto.CommentWithReplyCountResponse;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
  @Query(
      "SELECT new com.graduation.project.forum.dto.CommentWithReplyCountResponse("
          + "c.id, c.content, c.author.id, c.createdDateTime, COUNT(r), f.url) "
          + "FROM Comment c "
          + "LEFT JOIN c.replies r "
          + "LEFT JOIN FileMetadata f ON f.resourceId = c.id AND f.resourceType = 'COMMENT' "
          + "WHERE c.post.id = :postId AND c.parent IS NULL "
          + "GROUP BY c.id, c.content, c.author.id, c.createdDateTime, f.url")
  Page<CommentWithReplyCountResponse> findRootCommentsWithCount(
      @Param("postId") UUID postId, Pageable pageable);

  @Query(
      "SELECT c FROM Comment c "
          + "LEFT JOIN FETCH c.author "
          + // Fetch author để tránh lazy loading
          "WHERE c.parent.id = :parentId")
  Page<Comment> findRepliesByParentId(@Param("parentId") UUID parentId, Pageable pageable);

  @Modifying
  @Query("UPDATE Comment c SET c.reactionCount = c.reactionCount + 1 WHERE c.id = :commentId")
  void increaseReactionCount(@Param("commentId") UUID commentId);

  @Modifying
  @Query("UPDATE Comment c SET c.reactionCount = c.reactionCount - 1 WHERE c.id = :commentId")
  void decreaseReactionCount(@Param("commentId") UUID commentId);
}
