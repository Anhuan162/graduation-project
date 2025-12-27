package com.graduation.project.forum.service;

import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.entity.User;
import com.graduation.project.forum.constant.ReactionType;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.dto.ReactionDetailResponse;
import com.graduation.project.forum.dto.ReactionEvent;
import com.graduation.project.forum.dto.ReactionRequest;
import com.graduation.project.forum.dto.ReactionSummary;
import com.graduation.project.forum.entity.Reaction;
import com.graduation.project.forum.repository.CommentRepository;
import com.graduation.project.forum.repository.PostRepository;
import com.graduation.project.forum.repository.ReactionCountProjection;
import com.graduation.project.forum.repository.ReactionRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import java.time.LocalDateTime;
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

  private final ReactionRepository reactionRepository;
  private final CurrentUserService currentUserService;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final ApplicationEventPublisher publisher;

  /**
   * Toggle reaction:
   * - Nếu đã reaction cùng type -> delete (unlike)
   * - Nếu đã reaction khác type -> update type (không đổi count)
   * - Nếu chưa reaction -> create (like)
   */
  @Transactional
  public void toggleReaction(ReactionRequest request) {
    User user = currentUserService.getCurrentUserEntity();

    Optional<Reaction> existingOpt = reactionRepository.findByUserIdAndTargetIdAndTargetType(
        user.getId(), request.getTargetId(), request.getTargetType());

    // ===== Case: đã có reaction =====
    if (existingOpt.isPresent()) {
      Reaction existing = existingOpt.get();

      // A) bấm lại y hệt -> UNLIKE
      if (existing.getType() == request.getReactionType()) {
        reactionRepository.delete(existing);
        updateReactionCount(request.getTargetId(), request.getTargetType(), false);
        return;
      }

      // B) đổi reaction type -> UPDATE (không đổi count)
      existing.setType(request.getReactionType());
      reactionRepository.save(existing);
      return;
    }

    // ===== Case: chưa có reaction =====
    UUID receiverId = resolveReceiverId(request.getTargetType(), request.getTargetId());

    Reaction created = Reaction.builder()
        .user(user)
        .targetId(request.getTargetId())
        .targetType(request.getTargetType())
        .type(request.getReactionType())
        .createdAt(LocalDateTime.now())
        .build();

    reactionRepository.save(created);
    updateReactionCount(request.getTargetId(), request.getTargetType(), true);

    // Publish event nếu không self-like
    if (receiverId != null && !receiverId.equals(user.getId())) {
      publisher.publishEvent(ReactionEvent.from(created, user, receiverId));
    }
  }

  /**
   * Check nhanh isLiked cho 1 target (dùng cho detail).
   * (PostService có thể gọi trực tiếp repository cũng ok, nhưng helper này sạch
   * hơn)
   */
  @Transactional(readOnly = true)
  public boolean isReactedByMe(UUID targetId, TargetType targetType) {
    Optional<UUID> userIdOptional = currentUserService.getCurrentUserIdOptional();
    if (userIdOptional.isEmpty()) {
      return false;
    }
    return reactionRepository.existsByUserIdAndTargetIdAndTargetType(userIdOptional.get(), targetId, targetType);
  }

  /**
   * Batch map isLiked: Tránh N+1 cho list post/comment.
   * Trả về map: targetId -> boolean
   */
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
    // 1) Group by type
    List<ReactionCountProjection> projections = reactionRepository.countReactionsByTarget(targetId, targetType);

    Map<ReactionType, Long> counts = projections.stream()
        .collect(Collectors.toMap(ReactionCountProjection::getType, ReactionCountProjection::getCount));

    long total = counts.values().stream().mapToLong(Long::longValue).sum();

    // 2) current user reaction type (để highlight)
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

  private UUID resolveReceiverId(TargetType targetType, UUID targetId) {
    if (targetType == TargetType.POST) {
      return postRepository
          .findById(targetId)
          .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND))
          .getAuthor()
          .getId();
    }
    if (targetType == TargetType.COMMENT) {
      return commentRepository
          .findById(targetId)
          .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND))
          .getAuthor()
          .getId();
    }
    log.warn("Unknown TargetType: {}. Cannot resolve receiver.", targetType);
    return null;
    // Or throw: throw new AppException(ErrorCode.UNSUPPORTED_TARGET_TYPE);
  }

  private void updateReactionCount(UUID targetId, TargetType targetType, boolean isIncrement) {
    if (targetType == TargetType.POST) {
      if (isIncrement) {
        postRepository.increaseReactionCount(targetId);
      } else {
        int updatedRows = postRepository.decreaseReactionCount(targetId);
        if (updatedRows == 0) {
          log.warn(
              "Data Inconsistency: Attempted to decrease reaction count for Post {} but count was already 0 or Post not found.",
              targetId);
        }
      }
      return;
    }

    if (targetType == TargetType.COMMENT) {
      if (isIncrement) {
        commentRepository.increaseReactionCount(targetId);
      } else {
        int updatedRows = commentRepository.decreaseReactionCount(targetId);
        if (updatedRows == 0) {
          log.warn(
              "Data Inconsistency: Attempted to decrease reaction count for Comment {} but count was already 0 or Comment not found.",
              targetId);
        }
      }
    }
  }
}
