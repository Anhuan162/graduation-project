package com.graduation.project.forum.service;

import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.User;
import com.graduation.project.event.constant.EventType;
import com.graduation.project.event.dto.EventEnvelope;
import com.graduation.project.event.dto.NotificationEventDTO;
import com.graduation.project.event.producer.StreamProducer;
import com.graduation.project.forum.constant.TopicRole;
import com.graduation.project.forum.constant.TopicVisibility;
import com.graduation.project.forum.dto.TopicMemberResponse;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.entity.TopicMember;
import com.graduation.project.forum.repository.TopicMemberRepository;
import com.graduation.project.forum.repository.TopicRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
  private final AuthorizationService authorizationService;

  public List<TopicMemberResponse> getMembers(UUID topicId) {
    return topicMemberRepository.findByTopicId(topicId).stream()
        .map(TopicMemberResponse::toTopicMemberResponse)
        .toList();
  }

  @Transactional
  public TopicMemberResponse joinTopic(UUID topicId) {
    Topic topic = topicRepository
        .findById(topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    User user = currentUserService.getCurrentUserEntity();

    boolean exists = topicMemberRepository.existsByUserIdAndTopicId(user.getId(), topicId);
    if (exists) {
      throw new AppException(ErrorCode.TOPIC_MEMBER_EXISTED);
    }

    TopicMember topicMember = TopicMember.builder()
        .topic(topic)
        .user(user)
        .topicRole(TopicRole.MEMBER)
        .joinedAt(Instant.now())
        .build();

    if (topic.getTopicVisibility() == TopicVisibility.PUBLIC) {
      topicMember.setApproved(true);
      TopicMember saved = topicMemberRepository.save(topicMember);
      return TopicMemberResponse.toTopicMemberResponse(saved);
    }

    topicMember.setApproved(false);
    TopicMember saved = topicMemberRepository.save(topicMember);

    Set<UUID> managerUserIds = topicMemberRepository.findUserIdsByTopicIdAndRole(topicId, TopicRole.MANAGER);

    NotificationEventDTO dto = NotificationEventDTO.builder()
        .relatedId(saved.getId())
        .type(ResourceType.TOPIC_MEMBER)
        .title("Join Topic Request")
        .content(user.getFullName() + " wants to join topic: " + topic.getTitle())
        .senderId(user.getId())
        .senderName(user.getEmail())
        .receiverIds(managerUserIds)
        .createdAt(Instant.now())
        .build();

    EventEnvelope eventEnvelope = EventEnvelope.from(EventType.NOTIFICATION, dto, "TOPIC_MEMBER");
    streamProducer.publish(eventEnvelope);

    return TopicMemberResponse.toTopicMemberResponse(saved);
  }

  @Transactional
  public TopicMemberResponse approveJoin(UUID topicMemberId) {
    TopicMember tm = topicMemberRepository
        .findById(topicMemberId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_MEMBER_NOT_FOUND));

    User current = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canManageTopic(current, tm.getTopic())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    tm.setApproved(true);
    TopicMember saved = topicMemberRepository.save(tm);

    NotificationEventDTO dto = NotificationEventDTO.builder()
        .relatedId(topicMemberId)
        .type(ResourceType.TOPIC_MEMBER)
        .title("Request Approved")
        .content("Yêu cầu tham gia Topic " + tm.getTopic().getTitle() + " của bạn đã được chấp thuận.")
        .senderId(current.getId())
        .senderName(current.getEmail())
        .receiverIds(Set.of(tm.getUser().getId()))
        .createdAt(Instant.now())
        .build();

    EventEnvelope eventEnvelope = EventEnvelope.from(EventType.NOTIFICATION, dto, "TOPIC_MEMBER");
    streamProducer.publish(eventEnvelope);

    return TopicMemberResponse.toTopicMemberResponse(saved);
  }

  @Transactional
  public void kickMember(UUID topicId, UUID userId) {
    Topic topic = topicRepository
        .findById(topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    User current = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canManageTopic(current, topic)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    TopicMember tm = topicMemberRepository
        .findByUserIdAndTopicId(userId, topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_MEMBER_NOT_FOUND));

    topicMemberRepository.delete(tm);
  }

  public Page<TopicMemberResponse> findUnapprovedMember(Pageable pageable) {
    return topicMemberRepository.findAllByApprovedIsFalse(pageable)
        .map(TopicMemberResponse::toTopicMemberResponse);
  }

  public Page<TopicMemberResponse> findApprovedMember(Pageable pageable) {
    return topicMemberRepository.findAllByApprovedIsTrue(pageable)
        .map(TopicMemberResponse::toTopicMemberResponse);
  }

  @Transactional
  public void addTopicMember(UUID topicId, UUID userId, TopicRole topicRole) {
    Topic topic = topicRepository
        .findById(topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    User userToAdd = userRepository
        .findById(userId)
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canManageTopic(currentUser, topic)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    // Check if member already exists
    boolean existed = topicMemberRepository.existsByUserIdAndTopicId(userId, topicId);
    if (existed) {
      throw new AppException(ErrorCode.TOPIC_MEMBER_EXISTED);
    }

    TopicMember topicMember = TopicMember.builder()
        .topic(topic)
        .user(userToAdd)
        .topicRole(topicRole)
        .approved(true)
        .joinedAt(Instant.now())
        .build();

    topicMemberRepository.save(topicMember);
  }

  @Transactional
  public void removeTopicMember(UUID topicMemberId) {
    TopicMember topicMember = topicMemberRepository
        .findById(topicMemberId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_MEMBER_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canManageTopic(currentUser, topicMember.getTopic())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    topicMemberRepository.deleteById(topicMemberId);
  }

  @Transactional
  public void updateTopicMember(UUID topicMemberId, TopicRole topicRole) {
    TopicMember topicMember = topicMemberRepository
        .findById(topicMemberId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_MEMBER_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();

    if (!authorizationService.canManageTopic(currentUser, topicMember.getTopic())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    topicMember.setTopicRole(topicRole);
    topicMemberRepository.save(topicMember);
  }

  public boolean isTopicMember(UUID topicId, UUID userId) {
    return topicMemberRepository.existsByUserIdAndTopicIdAndApprovedTrue(userId, topicId);
  }

  public boolean canViewTopic(Topic topic, User user) {
    return authorizationService.canViewTopic(topic, user);
  }
}
