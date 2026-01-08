package com.graduation.project.forum.repository;

import com.graduation.project.forum.entity.Topic;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {
  Page<Topic> findByCategoryIdAndDeletedIsFalse(UUID categoryId, Pageable pageable);

  Page<Topic> findAll(Specification<Topic> spec, Pageable pageable);

  @org.springframework.data.jpa.repository.Query(
      "SELECT t FROM Topic t LEFT JOIN t.topicMembers m GROUP BY t ORDER BY COUNT(m) DESC")
  java.util.List<Topic> findTop10ByOrderByTopicMembersSizeDesc();
}
