package com.graduation.project.user.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.repository.TopicMemberRepository;
import com.graduation.project.common.repository.TopicRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TopicMemberService {

  private final TopicMemberRepository topicMemberRepository;
  private final TopicRepository topicRepository;
  private final CurrentUserService currentUserService;
  private final UserRepository userRepository;

  public List<TopicMember> getMembers(UUID topicId) {
    return topicMemberRepository.findByTopicId(topicId);
  }

  @Transactional
  public TopicMember joinTopic(UUID topicId) {
    Topic topic =
        topicRepository
            .findById(topicId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    boolean exists = topicMemberRepository.existsByUserIdAndTopicId(user.getId(), topicId);
    if (exists) {
      throw new AppException(ErrorCode.TOPIC_MEMBER_EXISTED);
    }

    if (topic.getTopicVisibility() == TopicVisibility.PUBLIC) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    TopicMember.TopicMemberBuilder builder =
        TopicMember.builder()
            .topic(topic)
            .user(user)
            .topicRole(TopicRole.MEMBER)
            .joinedAt(LocalDateTime.now());

    // PUBLIC → join ngay
    if (topic.getTopicVisibility() == TopicVisibility.PUBLIC) {
      builder.approved(true);
      return topicMemberRepository.save(builder.build());
    }

    // PRIVATE → pending approval
    builder.approved(false);
    return topicMemberRepository.save(builder.build());
  }

  @Transactional
  public TopicMember approveJoin(UUID topicMemberId) {
    TopicMember tm =
        topicMemberRepository
            .findById(topicMemberId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_MEMBER_NOT_FOUND));

    User current = currentUserService.getCurrentUserEntity();

    if (!canManageTopic(current, tm.getTopic())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    tm.setApproved(true);
    return topicMemberRepository.save(tm);
  }

  @Transactional
  public void kickMember(UUID topicId, UUID userId) {
    Topic topic =
        topicRepository
            .findById(topicId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    User current = currentUserService.getCurrentUserEntity();

    if (!canManageTopic(current, topic)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    TopicMember tm =
        topicMemberRepository
            .findByUserIdAndTopicId(userId, topicId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_MEMBER_NOT_FOUND));

    topicMemberRepository.delete(tm);
  }

  public Page<TopicMember> findUnapprovedMember(Pageable pageable) {
    Page<TopicMember> page = topicMemberRepository.findAllByApprovedIsFalse(pageable);
    return page;
  }

  public Page<TopicMember> findApprovedMember(Pageable pageable) {
    Page<TopicMember> page = topicMemberRepository.findAllByApprovedIsTrue(pageable);
    return page;
  }

  @Transactional
  public void addTopicMember(UUID topicId, UUID userId, String topicRole) {
    Topic topic =
        topicRepository
            .findById(topicId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    User userToAdd =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();

    // --- chỉ creator hoặc admin mới được add ---
    boolean canEditTopic = canManageTopic(currentUser, topic);

    if (!canEditTopic) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    TopicMember topicMember = new TopicMember();
    topicMember.setTopic(topic);
    topicMember.setUser(userToAdd);
    topicMember.setTopicRole(TopicRole.valueOf(topicRole));
    topicMember.setApproved(true);
    topicMember.setJoinedAt(LocalDateTime.now());

    topicMemberRepository.save(topicMember);
  }

  private static boolean canChangeManagerForTopic(User currentUser, Topic topic) {
    return currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))
        || topic.getCreatedBy().getId().equals(currentUser.getId());
  }

  @Transactional
  public void removeTopicMember(UUID topicMemberId) {
    TopicMember topicMember =
        topicMemberRepository
            .findById(topicMemberId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_MEMBER_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();

    boolean canManage = canChangeManagerForTopic(currentUser, topicMember.getTopic());
    if (!canManage) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    topicMemberRepository.deleteById(topicMemberId);
  }

  private boolean canManageTopic(User currentUser, Topic topic) {
    boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));
    boolean isCreator = topic.getCreatedBy().getId().equals(currentUser.getId());
    boolean isManager =
        topic.getTopicMembers().stream()
            .anyMatch(
                m ->
                    m.getUser().getId().equals(currentUser.getId())
                        && m.getTopicRole() == TopicRole.MANAGER);

    return isAdmin || isCreator || isManager;
  }

  public void updateTopicMember(UUID topicMemberId, TopicRole topicRole) {
    TopicMember topicMember =
        topicMemberRepository
            .findById(topicMemberId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
    User currentUser = currentUserService.getCurrentUserEntity();

    if (!canManageTopic(currentUser, topicMember.getTopic())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
    topicMember.setTopicRole(topicRole);
    topicMemberRepository.save(topicMember);
  }

  public boolean isTopicMember(UUID topicId, UUID userId) {
    return topicMemberRepository.existsByUserIdAndTopicIdAndApprovedIsTrue(userId, topicId);
  }

  public boolean canViewTopic(Topic topic, User user) {
    if (topic.getTopicVisibility() == TopicVisibility.PUBLIC) return true;
    return isTopicMember(topic.getId(), user.getId());
  }
}
