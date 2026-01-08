package com.graduation.project.forum.service;

import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.event.constant.EventType;
import com.graduation.project.forum.constant.TopicRole;
import com.graduation.project.forum.constant.TopicVisibility;
import com.graduation.project.forum.entity.Topic;
import com.graduation.project.forum.entity.TopicMember;
import com.graduation.project.forum.repository.TopicMemberRepository;
import com.graduation.project.forum.repository.TopicRepository;
import com.graduation.project.event.dto.EventEnvelope;
import com.graduation.project.event.dto.NotificationEventDTO;
import com.graduation.project.event.producer.StreamProducer;
import com.graduation.project.forum.dto.TopicMemberResponse;
import java.time.LocalDateTime;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
  private final com.graduation.project.event.service.NotificationHandler notificationHandler;
  private final AuthorizationService authorizationService;

  public Page<TopicMemberResponse> getMembers(UUID topicId, Boolean approved, Pageable pageable) {
    Page<TopicMember> page;
    if (approved != null) {
      page = topicMemberRepository.findByTopicIdAndApproved(topicId, approved, pageable);
    } else {
      page = topicMemberRepository.findByTopicId(topicId, pageable);
    }
    return page.map(TopicMemberResponse::toTopicMemberResponse);
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
        .joinedAt(LocalDateTime.now())
        .build();

    // PUBLIC → join ngay (approved = true)
    if (topic.getTopicVisibility() == TopicVisibility.PUBLIC) {
      topicMember.setApproved(true);
      return TopicMemberResponse.toTopicMemberResponse(topicMemberRepository.save(topicMember));
    }

    // PRIVATE → pending approval (approved = false)
    Set<UUID> managerIds = topic.getTopicMembers().stream()
        .filter(t -> t.getTopicRole().equals(TopicRole.MANAGER))
        .map(TopicMember::getId)
        .collect(Collectors.toSet());

    topicMember.setApproved(false);
    topicMemberRepository.save(topicMember);

    NotificationEventDTO dto = NotificationEventDTO.builder()
        .referenceId(topicId)
        .type(ResourceType.TOPIC)
        .relatedId(topicMember.getId())
        .title("Join Topic")
        .content(user.getFullName() + " wants to join your topic")
        .senderId(user.getId())
        .senderName(user.getFullName())
        .receiverIds(managerIds)
        .createdAt(LocalDateTime.now())
        .build();
    notificationHandler.handleNotification(dto);
    return TopicMemberResponse.toTopicMemberResponse(topicMember);
  }

  @Transactional
  public TopicMemberResponse approveJoin(UUID topicMemberId) {
    TopicMember tm = topicMemberRepository
        .findById(topicMemberId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_MEMBER_NOT_FOUND));

    User current = currentUserService.getCurrentUserEntity();

    if (!canManageTopic(current, tm.getTopic())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    tm.setApproved(true);
    TopicMember save = topicMemberRepository.save(tm);
    NotificationEventDTO dto = NotificationEventDTO.builder()
        .referenceId(tm.getTopic().getId())
        .type(ResourceType.TOPIC)
        .relatedId(topicMemberId)
        .title("Approve Member")
        .content("Bạn đã được chấp thuận là thành viên của Topic: " + tm.getTopic().getTitle())
        .senderId(current.getId())
        .senderName(current.getFullName())
        .receiverIds(Set.of(tm.getUser().getId()))
        .createdAt(LocalDateTime.now())
        .build();
    notificationHandler.handleNotification(dto);
    return TopicMemberResponse.toTopicMemberResponse(save);
  }

  @Transactional
  public void kickMember(UUID topicId, UUID userId) {
    Topic topic = topicRepository
        .findById(topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    User currentUser = currentUserService.getCurrentUserEntity();
    UUID currentUserId = currentUser.getId();

    // 1. Get Actor (Requester) Member Info
    TopicMember actor = topicMemberRepository
        .findByUserIdAndTopicId(currentUserId, topicId)
        .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED)); // Not even a member

    // 2. Get Target (Victim) Member Info
    TopicMember target = topicMemberRepository
        .findByUserIdAndTopicId(userId, topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_MEMBER_NOT_FOUND));

    // 3. Rule 1: No self-kicking
    if (actor.getId().equals(target.getId())) {
      throw new AppException(ErrorCode.BAD_REQUEST); // "Cannot kick yourself"
    }

    // 4. Rule 2: Protect Creator/Owner
    if (target.getTopicRole() == TopicRole.OWNER || topic.getCreatedBy().getId().equals(target.getUser().getId())) {
      throw new AppException(ErrorCode.UNAUTHORIZED); // "Cannot kick Creator"
    }

    // 5. Rule 3: Hierarchy Check (Actor must be > Target)
    boolean isActorAdmin = authorizationService.isAdmin(currentUser);
    boolean isActorCreator = topic.getCreatedBy().getId().equals(currentUserId);
    boolean isActorManager = actor.getTopicRole() == TopicRole.MANAGER;

    if (isActorAdmin || isActorCreator) {
      // Admin/Creator can kick anyone (except Creator, handled above)
      // Valid
    } else if (isActorManager) {
      // Manager cannot kick other Managers
      if (target.getTopicRole() == TopicRole.MANAGER) {
        throw new AppException(ErrorCode.UNAUTHORIZED); // "Manager cannot kick Manager"
      }
      // Manager cannot kick Creator (handled above)
      // Manager can kick Member -> OK
    } else {
      // Members cannot kick anyone
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    // 6. Execute Delete
    topicMemberRepository.delete(target);
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
  public void addTopicMember(UUID topicId, UUID userId, String topicRoleStr) {
    Topic topic = topicRepository
        .findById(topicId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));

    TopicRole roleToAdd;
    try {
      roleToAdd = TopicRole.valueOf(topicRoleStr);
    } catch (IllegalArgumentException e) {
      throw new AppException(ErrorCode.BAD_REQUEST);
    }

    User currentUser = currentUserService.getCurrentUserEntity();
    boolean isActorAdmin = authorizationService.isAdmin(currentUser);
    boolean isActorCreator = topic.getCreatedBy().getId().equals(currentUser.getId());

    // Check if actor is manager
    boolean isActorManager = topicMemberRepository.findByUserIdAndTopicId(currentUser.getId(), topicId)
        .map(tm -> tm.getTopicRole() == TopicRole.MANAGER)
        .orElse(false);

    // LOGIC CHECK
    if (isActorAdmin || isActorCreator) {
      // Pass
    } else if (isActorManager) {
      // Manager CANNOT add other Managers
      if (roleToAdd == TopicRole.MANAGER || roleToAdd == TopicRole.OWNER) {
        throw new AppException(ErrorCode.UNAUTHORIZED);
      }
      // Manager add Member -> OK
    } else {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    User userToAdd = userRepository
        .findById(userId)
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    if (topicMemberRepository.existsByUserIdAndTopicId(userId, topicId)) {
      throw new AppException(ErrorCode.TOPIC_MEMBER_EXISTED);
    }

    TopicMember topicMember = new TopicMember();
    topicMember.setTopic(topic);
    topicMember.setUser(userToAdd);
    topicMember.setTopicRole(roleToAdd);
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
    TopicMember topicMember = topicMemberRepository
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
    boolean isManager = topic.getTopicMembers().stream()
        .anyMatch(
            m -> m.getUser().getId().equals(currentUser.getId())
                && m.getTopicRole() == TopicRole.MANAGER);

    return isAdmin || isCreator || isManager;
  }

  @Transactional
  public void updateTopicMember(UUID topicMemberId, TopicRole newRole) {
    TopicMember targetMember = topicMemberRepository
        .findById(topicMemberId)
        .orElseThrow(() -> new AppException(ErrorCode.TOPIC_MEMBER_NOT_FOUND));

    Topic topic = targetMember.getTopic();
    User currentUser = currentUserService.getCurrentUserEntity();
    UUID currentUserId = currentUser.getId();

    boolean isActorAdmin = authorizationService.isAdmin(currentUser);
    boolean isActorCreator = topic.getCreatedBy().getId().equals(currentUserId);

    // 3. SECURITY CHECK: Only Admin or Creator can change roles
    if (!isActorAdmin && !isActorCreator) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    // 4. RULE: Cannot change Creator/Owner's role
    if (targetMember.getTopicRole() == TopicRole.OWNER
        || topic.getCreatedBy().getId().equals(targetMember.getUser().getId())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    // 5. RULE: Cannot demote self via this API (Safety check)
    if (targetMember.getUser().getId().equals(currentUserId)) {
      throw new AppException(ErrorCode.BAD_REQUEST);
    }

    targetMember.setTopicRole(newRole);
    topicMemberRepository.save(targetMember);
  }

  public boolean isTopicMember(UUID topicId, UUID userId) {
    return topicMemberRepository.existsByUserIdAndTopicIdAndApprovedIsTrue(userId, topicId);
  }

  public boolean canViewTopic(Topic topic, User user) {
    if (topic.getTopicVisibility() == TopicVisibility.PUBLIC)
      return true;
    return isTopicMember(topic.getId(), user.getId());
  }
}
