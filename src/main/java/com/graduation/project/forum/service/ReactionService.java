package com.graduation.project.forum.service;

import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.entity.User;
import com.graduation.project.forum.constant.ReactionType;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.dto.*;
import com.graduation.project.forum.entity.Comment;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.Reaction;
import com.graduation.project.forum.repository.CommentRepository;
import com.graduation.project.forum.repository.PostRepository;
import com.graduation.project.forum.repository.ReactionCountProjection;
import com.graduation.project.forum.repository.ReactionRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactionService {

  @PersistenceContext
  private EntityManager entityManager;

  private final ReactionRepository reactionRepository;
  private final CurrentUserService currentUserService;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final ApplicationEventPublisher publisher;

  @Transactional
  public ReactionToggleResponse toggleReaction(ReactionRequest request) {
    User user = currentUserService.getCurrentUserEntity();

    UUID targetId = request.getTargetId();
    TargetType targetType = request.getTargetType();

    Object targetEntity = validateTargetExists(targetType, targetId);

    // Initialize author before detaching to prevent LazyInitializationException
    if (targetEntity instanceof Post post) {
      post.getAuthor().getId();
    } else if (targetEntity instanceof Comment comment) {
      comment.getAuthor().getId();
    }

    entityManager.detach(targetEntity);

    Optional<Reaction> existingOpt = reactionRepository.findByUserIdAndTargetIdAndTargetType(user.getId(), targetId,
        targetType);

    boolean isReacted;
    ReactionType currentType;

    if (existingOpt.isPresent()) {
      Reaction existing = existingOpt.get();

      if (existing.getType() == request.getReactionType()) {
        // UNLIKE
        reactionRepository.deleteByIdExplicit(existing.getId());
        reactionRepository.flush();
        updateReactionCount(targetId, targetType, false);

        isReacted = false;
        currentType = null;
      } else {
        existing.setType(request.getReactionType());
        reactionRepository.save(existing);

        isReacted = true;
        currentType = request.getReactionType();
      }
    } else {
      Reaction created = Reaction.builder()
          .user(user)
          .targetId(targetId)
          .targetType(targetType)
          .type(request.getReactionType())
          .createdAt(Instant.now())
          .build();

      reactionRepository.saveAndFlush(created);
      updateReactionCount(targetId, targetType, true);

      notifyAuthor(targetEntity, user, created);

      isReacted = true;
      currentType = request.getReactionType();
    }

    long newCount = getLatestReactionCount(targetId, targetType);

    return ReactionToggleResponse.builder()
        .reacted(isReacted)
        .type(currentType)
        .totalReactions(newCount)
        .build();
  }

  private Object validateTargetExists(TargetType targetType, UUID targetId) {
    if (targetType == TargetType.POST) {
      return postRepository.findById(targetId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    }
    if (targetType == TargetType.COMMENT) {
      return commentRepository.findById(targetId).orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
    }
    throw new AppException(ErrorCode.UNSUPPORTED_TARGET_TYPE);
  }

  private long getLatestReactionCount(UUID targetId, TargetType targetType) {
    if (targetType == TargetType.POST) {
      return Optional.ofNullable(postRepository.getReactionCount(targetId)).orElse(0L);
    }
    if (targetType == TargetType.COMMENT) {
      return Optional.ofNullable(commentRepository.getReactionCount(targetId)).orElse(0L);
    }
    return 0L;
  }

  private void notifyAuthor(Object targetEntity, User actor, Reaction reaction) {
    UUID receiverId = null;

    if (targetEntity instanceof Post post) {
      receiverId = post.getAuthor().getId();
    } else if (targetEntity instanceof Comment comment) {
      receiverId = comment.getAuthor().getId();
    }

    if (receiverId != null && !receiverId.equals(actor.getId())) {
      publisher.publishEvent(ReactionEvent.from(reaction, actor, receiverId));
    }
  }

  @Transactional(readOnly = true)
  public boolean isReactedByMe(UUID targetId, TargetType targetType) {
    Optional<UUID> userIdOptional = currentUserService.getCurrentUserIdOptional();
    if (userIdOptional.isEmpty())
      return false;
    return reactionRepository.existsByUserIdAndTargetIdAndTargetType(userIdOptional.get(), targetId, targetType);
  }

  @Transactional(readOnly = true)
  public Map<UUID, Boolean> mapIsReactedByMe(List<UUID> targetIds, TargetType targetType) {
    Optional<UUID> userIdOptional = currentUserService.getCurrentUserIdOptional();
    if (userIdOptional.isEmpty() || targetIds == null || targetIds.isEmpty()) {
      return Collections.emptyMap();
    }

    UUID userId = userIdOptional.get();
    List<UUID> reactedIds = reactionRepository.findReactedTargetIdsByUser(userId, targetType, targetIds);
    Set<UUID> reactedSet = new HashSet<>(reactedIds);

    Map<UUID, Boolean> result = new HashMap<>();
    for (UUID id : targetIds) {
      result.put(id, reactedSet.contains(id));
    }
    return result;
  }

  @Transactional(readOnly = true)
  public ReactionSummary getReactionSummary(UUID targetId, TargetType targetType) {
    List<ReactionCountProjection> projections = reactionRepository.countReactionsByTarget(targetId, targetType);

    Map<ReactionType, Long> counts = projections.stream()
        .collect(Collectors.toMap(ReactionCountProjection::getType, ReactionCountProjection::getCount));

    long total = counts.values().stream().mapToLong(Long::longValue).sum();

    ReactionType currentUserReaction = null;
    Optional<UUID> userIdOptional = currentUserService.getCurrentUserIdOptional();
    if (userIdOptional.isPresent()) {
      currentUserReaction = reactionRepository
          .findByUserIdAndTargetIdAndTargetType(userIdOptional.get(), targetId, targetType)
          .map(Reaction::getType)
          .orElse(null);
    }

    return ReactionSummary.builder()
        .targetId(targetId)
        .counts(counts)
        .totalReactions(total)
        .userReaction(currentUserReaction)
        .build();
  }

  @Transactional(readOnly = true)
  public Page<ReactionDetailResponse> getReactions(
      UUID targetId, TargetType targetType, ReactionType filterType, Pageable pageable) {

    Page<Reaction> page = (filterType != null)
        ? reactionRepository.findAllByTargetIdAndTargetTypeAndType(targetId, targetType, filterType, pageable)
        : reactionRepository.findAllByTargetIdAndTargetType(targetId, targetType, pageable);

    return page.map(
        reaction -> ReactionDetailResponse.builder()
            .userId(reaction.getUser().getId())
            .username(reaction.getUser().getFullName())
            .avatarUrl(reaction.getUser().getAvatarUrl())
            .type(reaction.getType())
            .build());
  }

  private void updateReactionCount(UUID targetId, TargetType targetType, boolean isIncrement) {
    if (targetType == TargetType.POST) {
      if (isIncrement) {
        postRepository.updateReactionCount(targetId, 1);
      } else {
        int updatedRows = postRepository.decreaseReactionCount(targetId);
        if (updatedRows == 0) {
          log.warn("Attempted to decrease reaction count for Post {} but count was already 0.", targetId);
        }
      }
      return;
    }

    if (targetType == TargetType.COMMENT) {
      if (isIncrement) {
        commentRepository.updateReactionCount(targetId, 1);
      } else {
        int updatedRows = commentRepository.decreaseReactionCount(targetId);
        if (updatedRows == 0) {
          log.warn("Attempted to decrease reaction count for Comment {} but count was already 0.", targetId);
        }
      }
    }
  }
}
