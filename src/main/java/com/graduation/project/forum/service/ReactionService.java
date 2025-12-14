package com.graduation.project.forum.service;

import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.User;
import com.graduation.project.event.constant.EventType;
import com.graduation.project.event.dto.EventEnvelope;
import com.graduation.project.event.dto.NotificationEventDTO;
import com.graduation.project.event.producer.StreamProducer;
import com.graduation.project.forum.constant.ReactionType;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.dto.ReactionDetailResponse;
import com.graduation.project.forum.dto.ReactionRequest;
import com.graduation.project.forum.dto.ReactionSummary;
import com.graduation.project.forum.entity.Comment;
import com.graduation.project.forum.entity.Post;
import com.graduation.project.forum.entity.Reaction;
import com.graduation.project.forum.repository.CommentRepository;
import com.graduation.project.forum.repository.PostRepository;
import com.graduation.project.forum.repository.ReactionCountProjection;
import com.graduation.project.forum.repository.ReactionRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final StreamProducer streamProducer;

  @Transactional
  public void toggleReaction(ReactionRequest request) {
    User user = currentUserService.getCurrentUserEntity();
    var existingReactionOpt =
        reactionRepository.findByUserIdAndTargetIdAndTargetType(
            user.getId(), request.getTargetId(), request.getTargetType());

    if (existingReactionOpt.isPresent()) {
      Reaction existingReaction = existingReactionOpt.get();

      if (existingReaction.getType() == request.getType()) {
        // TRƯỜNG HỢP A: Đã thả rồi, bấm lại y hệt -> XÓA (Unlike)
        reactionRepository.delete(existingReaction);
        updateReactionCount(request.getTargetId(), request.getTargetType(), false);
      } else {
        // TRƯỜNG HỢP B: Đã thả rồi, nhưng đổi loại (Like -> Love) -> CẬP NHẬT
        existingReaction.setType(request.getType());
        reactionRepository.save(existingReaction);
      }
    } else {
      // TRƯỜNG HỢP C: Chưa thả bao giờ -> TẠO MỚI
      if (!postRepository.existsById(request.getTargetId())
          && request.getTargetType().equals(TargetType.POST)) {
        throw new AppException(ErrorCode.USER_EXISTED);
      }
      if (!commentRepository.existsById(request.getTargetId())
          && request.getTargetType().equals(TargetType.COMMENT)) {
        throw new AppException(ErrorCode.USER_EXISTED);
      }

      Reaction newReaction =
          Reaction.builder()
              .user(user)
              .targetId(request.getTargetId())
              .targetType(request.getTargetType())
              .type(request.getType())
              .build();

      reactionRepository.save(newReaction);
      updateReactionCount(request.getTargetId(), request.getTargetType(), true);

      sendReactionNotification(user, request);
    }
  }

  private void updateReactionCount(UUID targetId, TargetType targetType, boolean isIncrement) {
    if (targetType == TargetType.POST) {
      if (isIncrement) {
        postRepository.increaseReactionCount(targetId);
      } else {
        postRepository.decreaseReactionCount(targetId);
      }
    } else if (targetType == TargetType.COMMENT) {
      if (isIncrement) {
        commentRepository.increaseReactionCount(targetId);
      } else {
        commentRepository.decreaseReactionCount(targetId);
      }
    }
  }

  private void sendReactionNotification(User sender, ReactionRequest request) {
    try {
      User receiver = null;
      String content = "";
      String title = "";

      // 3. Phải query để lấy người nhận (Chủ bài viết hoặc chủ comment)
      if (request.getTargetType() == TargetType.POST) {
        Optional<Post> postOpt = postRepository.findById(request.getTargetId());
        if (postOpt.isPresent()) {
          receiver = postOpt.get().getAuthor(); // Giả sử Post có field createdBy
          title = "Ai đó đã bày tỏ cảm xúc về bài viết của bạn";
          content =
              sender.getFullName() + " đã thả " + request.getType() + " vào bài viết của bạn.";
        }
      } else if (request.getTargetType() == TargetType.COMMENT) {
        Optional<Comment> commentOpt = commentRepository.findById(request.getTargetId());
        if (commentOpt.isPresent()) {
          receiver = commentOpt.get().getAuthor(); // Giả sử Comment có field createdBy
          title = "Ai đó đã bày tỏ cảm xúc về bình luận của bạn";
          content =
              sender.getFullName() + " đã thả " + request.getType() + " vào bình luận của bạn.";
        }
      }

      // 4. Validate và tránh tự like tự sướng (Self-notification)
      if (receiver != null && !receiver.getId().equals(sender.getId())) {

        NotificationEventDTO dto =
            NotificationEventDTO.builder()
                .relatedId(request.getTargetId()) // ID bài viết hoặc comment
                .type(ResourceType.REACTION) // Hoặc tạo thêm NotificationType.REACTION
                .title(title)
                .content(content)
                .senderId(sender.getId())
                .senderName(sender.getFullName()) // Hoặc email
                .receiverIds(Collections.singleton(receiver.getId())) // List chứa 1 người
                .createdAt(java.time.LocalDateTime.now())
                .build();

        // 5. Đóng gói Event và đẩy vào Redis
        EventEnvelope eventEnvelope =
            EventEnvelope.from(EventType.NOTIFICATION, dto, "REACTION_SERVICE");

        streamProducer.publish(eventEnvelope);
      }

    } catch (Exception e) {
      // Log lỗi nhưng KHÔNG throw exception để tránh rollback transaction của việc Like
      log.error("Failed to send reaction notification", e);
    }
  }

  @Transactional(readOnly = true)
  public ReactionSummary getReactionSummary(UUID targetId, TargetType targetType) {
    // 1. Lấy thống kê số lượng (Group by Type)
    List<ReactionCountProjection> projections =
        reactionRepository.countReactionsByTarget(targetId, targetType);

    // Convert List Projection sang Map<Type, Long>
    Map<ReactionType, Long> counts =
        projections.stream()
            .collect(
                Collectors.toMap(
                    ReactionCountProjection::getType, ReactionCountProjection::getCount));

    // Tính tổng số reaction
    long total = counts.values().stream().mapToLong(Long::longValue).sum();

    // 2. Kiểm tra user hiện tại đã thả gì chưa (để highlight nút like)
    ReactionType currentUserReaction = null;
    try {
      UUID currentUserId = currentUserService.getCurrentUserId();
      // Nếu user chưa login thì currentUserId có thể null hoặc throw exception, cần handle tùy
      // logic auth của bạn
      if (currentUserId != null) {
        Optional<Reaction> myReaction =
            reactionRepository.findByUserIdAndTargetIdAndTargetType(
                currentUserId, targetId, targetType);
        if (myReaction.isPresent()) {
          currentUserReaction = myReaction.get().getType();
        }
      }
    } catch (Exception e) {
      // User chưa login, bỏ qua
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
    Page<Reaction> page;

    if (filterType != null) {
      page =
          reactionRepository.findAllByTargetIdAndTargetTypeAndType(
              targetId, targetType, filterType, pageable);
    } else {
      page = reactionRepository.findAllByTargetIdAndTargetType(targetId, targetType, pageable);
    }

    // Map Entity sang DTO
    return page.map(
        reaction ->
            ReactionDetailResponse.builder()
                .userId(reaction.getUser().getId())
                .username(reaction.getUser().getFullName()) // Giả sử User entity có field này
                //                .avatarUrl(reaction.getUser().getAvatarUrl()) // Giả sử User
                .type(reaction.getType())
                .build());
  }
}
