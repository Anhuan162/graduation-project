package com.graduation.project.forum.repository;

import com.graduation.project.forum.constant.PostStatus;
import com.graduation.project.forum.entity.Post;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
  Page<Post> findByTopicId(UUID topicId, Pageable pageable);

  List<Post> findByTopicIdAndPostStatus(UUID topicId, PostStatus postStatus);

  @Modifying
  @Query("UPDATE Post p SET p.reactionCount = p.reactionCount + 1 WHERE p.id = :postId")
  void increaseReactionCount(UUID postId);

  @Modifying
  @Query("UPDATE Post p SET p.reactionCount = p.reactionCount - 1 WHERE p.id = :postId")
  void decreaseReactionCount(UUID postId);
}
