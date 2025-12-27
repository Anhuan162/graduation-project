package com.graduation.project.forum.repository;

import com.graduation.project.forum.entity.Comment;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID>, JpaSpecificationExecutor<Comment> {

  @EntityGraph(attributePaths = { "author" })
  @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.rootComment IS NULL AND c.deleted = false")
  Page<Comment> findRootComments(@Param("postId") UUID postId, Pageable pageable);

  @EntityGraph(attributePaths = { "author", "replyToUser" })
  @Query("SELECT c FROM Comment c WHERE c.rootComment.id = :rootId AND c.deleted = false ORDER BY c.createdDateTime ASC")
  List<Comment> findAllRepliesByRootId(@Param("rootId") UUID rootId);

  @EntityGraph(attributePaths = { "author", "replyToUser" })
  @Query("SELECT c FROM Comment c WHERE c.rootComment.id = :rootId AND c.deleted = false ORDER BY c.createdDateTime ASC")
  Page<Comment> findAllRepliesByRootId(@Param("rootId") UUID rootId, Pageable pageable);

  @Query("SELECT c.rootComment.id, COUNT(c) FROM Comment c " +
      "WHERE c.rootComment.id IN :rootIds AND c.deleted = false " +
      "GROUP BY c.rootComment.id")
  List<Object[]> countRepliesByRootIds(@Param("rootIds") List<UUID> rootIds);

  @Modifying
  @Query("UPDATE Comment c SET c.reactionCount = c.reactionCount + 1 WHERE c.id = :commentId")
  void increaseReactionCount(@Param("commentId") UUID commentId);

  @Modifying
  @Query("UPDATE Comment c SET c.reactionCount = c.reactionCount - 1 WHERE c.id = :commentId AND c.reactionCount > 0")
  int decreaseReactionCount(@Param("commentId") UUID commentId);

  @EntityGraph(attributePaths = { "post" })
  Page<Comment> findAllByAuthorIdAndDeletedFalse(UUID authorId, Pageable pageable);

  @Query("SELECT COUNT(c) FROM Comment c WHERE c.rootComment.id = :rootId AND c.deleted = false")
  long countByRootCommentId(@Param("rootId") UUID rootId);
}
