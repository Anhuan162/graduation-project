package com.graduation.project.forum.repository;

import com.graduation.project.forum.entity.Topic;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {

  Page<Topic> findByCategoryIdAndDeletedIsFalse(UUID categoryId, Pageable pageable);

  Page<Topic> findAll(Specification<Topic> spec, Pageable pageable);

  @Query("SELECT t.id FROM Topic t WHERE t.deleted = false AND " +
      "(t.topicVisibility = com.graduation.project.forum.constant.TopicVisibility.PUBLIC OR " +
      "EXISTS (SELECT 1 FROM TopicMember tm WHERE tm.topic.id = t.id AND tm.user.id = :userId AND tm.approved = true) OR "
      +
      "EXISTS (SELECT 1 FROM User u JOIN u.roles r WHERE u.id = :userId AND r.name = 'ADMIN'))")
  Set<UUID> findAccessibleTopicIdsByUserId(@Param("userId") UUID userId);

  @Query("""
        SELECT DISTINCT t FROM Topic t
        JOIN TopicMember tm ON tm.topic.id = t.id
        WHERE t.deleted = false
          AND tm.user.id = :userId
          AND tm.approved = true
        ORDER BY t.createdAt DESC
      """)
  Page<Topic> findPostableTopicsByUser(@Param("userId") UUID userId, Pageable pageable);

  Page<Topic> findAllByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

}
