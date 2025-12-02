package com.graduation.project.forum.service;

import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.event.constant.EventType;
import com.graduation.project.event.constant.NotificationType;
import com.graduation.project.forum.constant.TopicRole;
import com.graduation.project.forum.constant.TopicVisibility;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.entity.TopicMember;
import com.graduation.project.forum.repository.TopicMemberRepository;
import com.graduation.project.forum.repository.TopicRepository;
import com.graduation.project.event.dto.EventEnvelope;
import com.graduation.project.event.dto.NotificationMessageDTO;
import com.graduation.project.event.producer.StreamProducer;
import com.graduation.project.forum.dto.TopicMemberResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.graduation.project.common.entity.User;
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
  private final StreamProducer streamProducer;

  public List<TopicMemberResponse> getMembers(UUID topicId) {
    return topicMemberRepository.findByTopicId(topicId).stream()
        .map(TopicMemberResponse::toTopicMemberResponse)
        .toList();
  }

  @Transactional
  public TopicMemberResponse joinTopic(UUID topicId) {
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

    TopicMember topicMember =
        TopicMember.builder()
            .topic(topic)
            .user(user)
            .topicRole(TopicRole.MEMBER)
            .joinedAt(LocalDateTime.now())
            .build();

    // PUBLIC → join ngay
    if (topic.getTopicVisibility() == TopicVisibility.PUBLIC) {
      topicMember.setApproved(true);
      return TopicMemberResponse.toTopicMemberResponse(topicMemberRepository.save(topicMember));
    }
    List<UUID> managerIds =
        topic.getTopicMembers().stream()
            .filter(t -> t.getTopicRole().equals(TopicRole.MANAGER))
            .map(TopicMember::getId)
            .toList();
    // PRIVATE → pending approval
    topicMember.setApproved(false);
    topicMemberRepository.save(topicMember);
    NotificationMessageDTO dto =
        NotificationMessageDTO.builder()
            .relatedId(topicMember.getId())
            .type(NotificationType.TOPIC_MEMBER)
            .title("Join Topic")
            .content(user.getFullName() + "want to join your topic")
            .senderId(user.getId())
            .senderName(user.getEmail())
            .receiverIds(managerIds) // build list of UUID receivers
            .createdAt(LocalDateTime.now())
            .build();
    EventEnvelope eventEnvelope = EventEnvelope.from(EventType.NOTIFICATION, dto, "TOPIC_MEMBER");
    streamProducer.publish(eventEnvelope);
    return TopicMemberResponse.toTopicMemberResponse(topicMember);
  }

  @Transactional
  public TopicMemberResponse approveJoin(UUID topicMemberId) {
    TopicMember tm =
        topicMemberRepository
            .findById(topicMemberId)
            .orElseThrow(() -> new AppException(ErrorCode.TOPIC_MEMBER_NOT_FOUND));

    User current = currentUserService.getCurrentUserEntity();

    if (!canManageTopic(current, tm.getTopic())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    tm.setApproved(true);
    TopicMember save = topicMemberRepository.save(tm);
    NotificationMessageDTO dto =
        NotificationMessageDTO.builder()
            .relatedId(topicMemberId)
            .type(NotificationType.TOPIC_MEMBER)
            .title("Approve Member")
            .content("Bạn đã được chấp thuận là thành viên của Topic")
            .senderId(current.getId())
            .senderName(current.getEmail())
            .receiverIds(List.of(tm.getUser().getId())) // build list of UUID receivers
            .createdAt(LocalDateTime.now())
            .build();
    EventEnvelope eventEnvelope = EventEnvelope.from(EventType.NOTIFICATION, dto, "TOPIC_MEMBER");
    streamProducer.publish(eventEnvelope);
    return TopicMemberResponse.toTopicMemberResponse(save);
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

  public Page<TopicMemberResponse> findUnapprovedMember(Pageable pageable) {
    Page<TopicMember> page = topicMemberRepository.findAllByApprovedIsFalse(pageable);
    return page.map(TopicMemberResponse::toTopicMemberResponse);
  }

  public Page<TopicMemberResponse> findApprovedMember(Pageable pageable) {
    Page<TopicMember> page = topicMemberRepository.findAllByApprovedIsTrue(pageable);
    return page.map(TopicMemberResponse::toTopicMemberResponse);
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
