package com.graduation.project.forum.repository;

import com.graduation.project.forum.constant.TopicRole;
import com.graduation.project.forum.entity.TopicMember;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicMemberRepository extends JpaRepository<TopicMember, UUID> {

        List<TopicMember> findByTopicId(UUID topicId);

        boolean existsByUserIdAndTopicIdAndApprovedTrue(UUID userId, UUID topicId);

        boolean existsByUserIdAndTopicIdAndTopicRoleAndApprovedTrue(UUID userId, UUID topicId, TopicRole topicRole);

        boolean existsByUserIdAndTopicId(UUID userId, UUID topicId);

        Optional<TopicMember> findByUserIdAndTopicId(UUID userId, UUID topicId);

        Page<TopicMember> findAllByApprovedIsFalse(Pageable pageable);

        Page<TopicMember> findAllByApprovedIsTrue(Pageable pageable);

        void deleteByUserIdAndTopicId(UUID userId, UUID topicId);

        @Query("SELECT COUNT(tm) > 0 FROM TopicMember tm " +
                        "WHERE tm.user.id = :userId AND tm.topic.id = :topicId " +
                        "AND (:role IS NULL OR tm.topicRole = :role) " +
                        "AND tm.approved = true")
        boolean checkPermission(@Param("userId") UUID userId,
                        @Param("topicId") UUID topicId,
                        @Param("role") TopicRole role);
}