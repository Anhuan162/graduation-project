package com.graduation.project.forum.repository;

import com.graduation.project.forum.entity.TopicMember;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicMemberRepository extends JpaRepository<TopicMember, UUID> {
  List<TopicMember> findByTopicId(UUID topicId);

  boolean existsByUserIdAndTopicId(UUID id, UUID topicId);

  Optional<TopicMember> findByUserIdAndTopicId(UUID userId, UUID topicId);

  Page<TopicMember> findAllByApprovedIsFalse(Pageable pageable);

  Page<TopicMember> findAllByApprovedIsTrue(Pageable pageable);

  boolean existsByUserIdAndTopicIdAndApprovedIsTrue(UUID userId, UUID topicId);
}
