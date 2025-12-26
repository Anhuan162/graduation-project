package com.graduation.project.forum.repository;

import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.entity.Post;

import java.util.List;
import java.util.UUID;

import com.graduation.project.forum.dto.PostAcceptedFilterRequest;
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

  @Modifying
  @Query("UPDATE Post p SET p.reactionCount = p.reactionCount + 1 WHERE p.id = :postId")
  void increaseReactionCount(UUID postId);

  @Modifying
  @Query("UPDATE Post p SET p.reactionCount = p.reactionCount - 1 WHERE p.id = :postId")
  void decreaseReactionCount(UUID postId);

  Page<Post> findAll(Specification<Post> spec, Pageable pageable);

  Page<Post> findAllByAuthor_Id(UUID userId, Pageable pageable);

  @Query("SELECT DISTINCT p FROM Post p " +
          "LEFT JOIN FETCH p.comments c " +
          "WHERE (:#{#post.title} IS NULL OR p.title LIKE CONCAT('%', :#{#post.title}, '%')) AND " +
          "(:#{#post.reactionCount} IS NULL OR p.reactionCount >= :#{#post.reactionCount}) AND " +
          "( :#{#post.timeBegin} IS NULL OR p.createdDateTime >= :#{#post.timeBegin}) AND " +
          "(:#{#post.timeEnd} IS NULL OR p.createdDateTime <= :#{#post.timeEnd}) AND " +
          " (:#{#post.topicId} is null or :#{#post.topicId} = p.topic.id) and  " +
          "(p.postStatus = com.graduation.project.forum.constant.PostStatus.APPROVED) AND " +
          "(c IS NULL OR c.isAccepted = true)")
  List<Post> getPostAccepted(@Param("post") PostAcceptedFilterRequest postAcceptedRequest);
}
