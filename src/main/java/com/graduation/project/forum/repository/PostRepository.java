package com.graduation.project.forum.repository;

import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.dto.PostAcceptedFilterRequest;
import com.graduation.project.forum.dto.PostStatDTO;
import com.graduation.project.forum.entity.Post;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

  Page<Post> findByTopicIdAndPostStatusAndDeletedFalse(
      UUID topicId, PostStatus postStatus, Pageable pageable);

  // For managers - see all posts in topic
  Page<Post> findByTopicIdAndDeletedFalse(UUID topicId, Pageable pageable);

  // For regular users - Ghost Post Pattern
  // Shows: APPROVED posts + user's own PENDING/REJECTED posts
  @Query(
      "SELECT p FROM Post p WHERE p.topic.id = :topicId AND p.deleted = false AND "
          + "(p.postStatus = 'APPROVED' OR p.author.id = :userId) "
          + "ORDER BY p.createdDateTime DESC")
  Page<Post> findPublicAndOwnPosts(
      @Param("topicId") UUID topicId, @Param("userId") UUID userId, Pageable pageable);

  @Modifying(clearAutomatically = true)
  @Query(
      "UPDATE Post p SET p.reactionCount = COALESCE(p.reactionCount, 0) + 1 WHERE p.id = :postId")
  void increaseReactionCount(UUID postId);

  @Modifying(clearAutomatically = true)
  @Query(
      "UPDATE Post p SET p.reactionCount = CASE WHEN COALESCE(p.reactionCount, 0) > 0 THEN p.reactionCount - 1 ELSE 0 END WHERE p.id = :postId")
  void decreaseReactionCount(UUID postId);

  Page<Post> findAll(Specification<Post> spec, Pageable pageable);

  Page<Post> findAllByAuthor_Id(UUID userId, Pageable pageable);

  @Query(
      "SELECT DISTINCT p FROM Post p "
          + "LEFT JOIN FETCH p.comments c "
          + "LEFT JOIN FETCH p.author "
          + "LEFT JOIN FETCH c.author "
          + "WHERE (:#{#post.title} IS NULL OR p.title LIKE CONCAT('%', :#{#post.title}, '%')) AND "
          + "(:#{#post.reactionCount} IS NULL OR p.reactionCount >= :#{#post.reactionCount}) AND "
          + "( :#{#post.timeBegin} IS NULL OR p.createdDateTime >= :#{#post.timeBegin}) AND "
          + "(:#{#post.timeEnd} IS NULL OR p.createdDateTime <= :#{#post.timeEnd}) AND "
          + " (:#{#post.topicId} is null or :#{#post.topicId} = p.topic.id) and  "
          + "(p.postStatus = com.graduation.project.forum.constant.PostStatus.APPROVED) AND "
          + "(c IS NULL OR c.deleted = false)")
  List<Post> getPostAccepted(@Param("post") PostAcceptedFilterRequest postAcceptedRequest);

  List<Post> findTop10ByOrderByReactionCountDesc();

  // Trong file PostRepository.java

  // SỬA: Dùng 'to_char' và format 'YYYY-MM-DD' cho PostgreSQL
  @Query(
      "SELECT new com.graduation.project.forum.dto.PostStatDTO(FUNCTION('to_char', p.createdDateTime, 'YYYY-MM-DD'), COUNT(p)) "
          + "FROM Post p "
          + "WHERE p.createdDateTime >= :startDate "
          + "GROUP BY FUNCTION('to_char', p.createdDateTime, 'YYYY-MM-DD') "
          + "ORDER BY FUNCTION('to_char', p.createdDateTime, 'YYYY-MM-DD') ASC")
  List<PostStatDTO> countPostsByDate(@Param("startDate") java.time.LocalDateTime startDate);

  // SỬA: Dùng 'to_char' và format 'YYYY-MM' cho PostgreSQL
  @Query(
      "SELECT new com.graduation.project.forum.dto.PostStatDTO(FUNCTION('to_char', p.createdDateTime, 'YYYY-MM'), COUNT(p)) "
          + "FROM Post p "
          + "WHERE p.createdDateTime >= :startDate "
          + "GROUP BY FUNCTION('to_char', p.createdDateTime, 'YYYY-MM') "
          + "ORDER BY FUNCTION('to_char', p.createdDateTime, 'YYYY-MM') ASC")
  List<PostStatDTO> countPostsByMonth(@Param("startDate") java.time.LocalDateTime startDate);
}
