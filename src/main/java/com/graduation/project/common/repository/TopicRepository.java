package com.graduation.project.common.repository;

import com.graduation.project.common.entity.Topic;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {
  List<Topic> findByCategoryId(UUID categoryId);
}
