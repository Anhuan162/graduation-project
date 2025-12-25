package com.graduation.project.forum.repository;

import com.graduation.project.forum.dto.CommentWithReplyCountResponse;
import com.graduation.project.forum.entity.Comment;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, UUID>, JpaSpecificationExecutor<Comment> {

        @Query("SELECT new com.graduation.project.forum.dto.CommentWithReplyCountResponse("
                        + "c.id, c.content, c.author.id, c.createdDateTime, COUNT(r), f.url) "
                        + "FROM Comment c "
                        + "LEFT JOIN c.replies r "
                        + "LEFT JOIN FileMetadata f ON f.resourceId = c.id AND f.resourceType = 'COMMENT' "
                        + "WHERE c.post.id = :postId AND c.parent IS NULL AND c.deleted IS FALSE "
                        + "GROUP BY c.id, c.content, c.author.id, c.createdDateTime, f.url")
        Page<CommentWithReplyCountResponse> findRootCommentsWithCount(
                        @Param("postId") UUID postId, Pageable pageable);

        @EntityGraph(attributePaths = { "author" })
        @Query(value = "SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL AND c.deleted IS FALSE", countQuery = "SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL AND c.deleted IS FALSE")
        Page<Comment> findRootCommentsEntity(@Param("postId") UUID postId, Pageable pageable);

        @Query(value = """
                        SELECT c FROM Comment c
                        LEFT JOIN FETCH c.author
                        WHERE c.parent.id = :parentId AND c.deleted IS FALSE
                        """, countQuery = "SELECT COUNT(c) FROM Comment c WHERE c.parent.id = :parentId AND c.deleted IS FALSE")
        Page<Comment> findRepliesByParentId(@Param("parentId") UUID parentId, Pageable pageable);

        // ====== counters ======
        @Modifying
        @Query("UPDATE Comment c SET c.reactionCount = c.reactionCount + 1 WHERE c.id = :commentId")
        void increaseReactionCount(@Param("commentId") UUID commentId);

        @Modifying
        @Query("UPDATE Comment c SET c.reactionCount = c.reactionCount - 1 WHERE c.id = :commentId")
        void decreaseReactionCount(@Param("commentId") UUID commentId);

        // ====== my comments ======
        Page<Comment> findAllByAuthorIdAndDeletedFalse(UUID id, Pageable pageable);

        @Query("""
                          SELECT c.parent.id, COUNT(c)
                          FROM Comment c
                          WHERE c.parent.id IN :parentIds
                            AND c.deleted IS FALSE
                          GROUP BY c.parent.id
                        """)
        List<Object[]> countRepliesByParentIds(@Param("parentIds") List<UUID> parentIds);

}
