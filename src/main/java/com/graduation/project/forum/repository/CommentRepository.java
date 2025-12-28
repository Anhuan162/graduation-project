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
  @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.rootComment IS NULL")
  Page<Comment> findRootComments(@Param("postId") UUID postId, Pageable pageable);

  @EntityGraph(attributePaths = { "author", "replyToUser" })
  @Query("SELECT c FROM Comment c WHERE c.rootComment.id = :rootId ORDER BY c.createdDateTime ASC")
  List<Comment> findAllRepliesByRootId(@Param("rootId") UUID rootId);

  @EntityGraph(attributePaths = { "author", "replyToUser" })
  @Query("SELECT c FROM Comment c WHERE c.rootComment.id = :rootId AND c.deleted = false ORDER BY c.createdDateTime ASC")
  Page<Comment> findAllRepliesByRootId(@Param("rootId") UUID rootId, Pageable pageable);

  @Query("SELECT c.rootComment.id, COUNT(c) FROM Comment c " +
      "WHERE c.rootComment.id IN :rootIds AND c.deleted = false " +
      "GROUP BY c.rootComment.id")
  List<Object[]> countRepliesByRootIds(@Param("rootIds") List<UUID> rootIds);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE Comment c SET c.reactionCount = c.reactionCount + 1 WHERE c.id = :id")
  void increaseReactionCount(@Param("id") UUID id);

  @Modifying
  @Query("UPDATE Comment c SET c.reactionCount = c.reactionCount - 1 WHERE c.id = :id AND c.reactionCount > 0")
  int decreaseReactionCount(@Param("id") UUID id);

  @Query("SELECT c.reactionCount FROM Comment c WHERE c.id = :id")
  Long getReactionCount(@Param("id") UUID id);

  @EntityGraph(attributePaths = { "post" })
  Page<Comment> findAllByAuthorIdAndDeletedFalse(UUID authorId, Pageable pageable);

  @Query("SELECT COUNT(c) FROM Comment c WHERE c.rootComment.id = :rootId AND c.deleted = false")
  long countByRootCommentId(@Param("rootId") UUID rootId);

  @Query("SELECT COUNT(c) FROM Comment c WHERE c.parent.id = :parentId")
  long countByParentId(@Param("parentId") UUID parentId);
}
