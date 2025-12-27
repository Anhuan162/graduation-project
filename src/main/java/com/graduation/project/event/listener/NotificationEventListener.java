package com.graduation.project.event.listener;

import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.event.constant.EventType;
import com.graduation.project.event.dto.EventEnvelope;
import com.graduation.project.event.dto.NotificationEventDTO;
import com.graduation.project.event.producer.StreamProducer;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.dto.CreatedCommentEvent;
import com.graduation.project.forum.dto.ReactionEvent;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

  private final StreamProducer streamProducer;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async
  public void handleCommentEvent(CreatedCommentEvent event) {
    UUID authorId = event.getAuthorId();
    UUID postOwnerId = event.getPostOwnerId();
    UUID parentOwnerId = event.getParentCommentOwnerId();

    if (!authorId.equals(postOwnerId)) {
      String title = event.getSenderName() + " đã bình luận về bài viết của bạn";
      publishNotification(
          event.getId(),
          ResourceType.COMMENT,
          event.getParentCommentId(),
          event.getPostId(),
          title,
          event.getContent(),
          authorId,
          event.getSenderName(),
          postOwnerId,
          event.getCreatedDateTime());
    }

    if (parentOwnerId != null
        && !authorId.equals(parentOwnerId)
        && !parentOwnerId.equals(postOwnerId)) {

      String title = event.getSenderName() + " đã trả lời bình luận của bạn";
      publishNotification(
          event.getId(),
          ResourceType.COMMENT,
          event.getParentCommentId(),
          event.getPostId(),
          title,
          event.getContent(),
          authorId,
          event.getSenderName(),
          parentOwnerId,
          event.getCreatedDateTime());
    }
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async
  public void handleReactionNotificationEvent(ReactionEvent event) {
    try {
      if (event.getSenderId().equals(event.getReceiverId())) {
        return;
      }

      String action = "";
      String targetName = "";

      if (event.getTargetType() == TargetType.POST) {
        action = "bài viết";
      } else {
        action = "bình luận";
      }

      String title = String.format("%s đã bày tỏ cảm xúc về %s của bạn", event.getSenderName(), action);
      String content = String.format("%s đã thả %s vào %s của bạn.", event.getSenderName(), event.getType(), action);

      publishNotification(
          null,
          ResourceType.REACTION,
          null,
          event.getTargetId(),
          title,
          content,
          event.getSenderId(),
          event.getSenderName(),
          event.getReceiverId(),
          Instant.now() // 🛠 FIX: LocalDateTime -> Instant
      );

    } catch (Exception e) {
      log.error("Failed to send reaction notification for target: {}", event.getTargetId(), e);
    }
  }

  private void publishNotification(
      UUID referenceId,
      ResourceType type,
      UUID parentReferenceId,
      UUID relatedId,
      String title,
      String content,
      UUID senderId,
      String senderName,
      UUID receiverId,
      Instant createdAt) {

    NotificationEventDTO dto = NotificationEventDTO.builder()
        .referenceId(referenceId)
        .type(type)
        .parentReferenceId(parentReferenceId)
        .relatedId(relatedId)
        .title(title)
        .content(content)
        .senderId(senderId)
        .senderName(senderName)
        .receiverIds(Set.of(receiverId))
        .createdAt(createdAt)
        .build();

    EventEnvelope eventEnvelope = EventEnvelope.from(EventType.NOTIFICATION, dto, type.name());
    streamProducer.publish(eventEnvelope);
  }
}