package com.graduation.project.event.listener;

import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.event.constant.EventType;
import com.graduation.project.event.dto.EventEnvelope;
import com.graduation.project.event.dto.NotificationEventDTO; // DTO riêng cho thông báo
import com.graduation.project.event.producer.StreamProducer;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.dto.CreatedCommentEvent;
import com.graduation.project.forum.dto.ReactionEvent;
import com.graduation.project.event.service.NotificationHandler;
import com.graduation.project.common.event.NotificationEvent;
import com.graduation.project.common.event.NotificationType;
import org.springframework.context.event.EventListener;
import java.util.Collections;
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
  private final NotificationHandler notificationHandler;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async
  public void handleCommentEvent(CreatedCommentEvent event) {
    if (!event.getAuthorId().equals(event.getParentCommentOwnerId())) {
      String title = event.getSenderName() + " đã bình luận bài viết của bạn";
      UUID recipientId = event.getPostOwnerId();
      publishCommentEvent(event, title, recipientId);
    }

    // -- TRƯỜNG HỢP 2: Gửi cho chủ Post --
    // Điều kiện:
    // 1. Người trả lời không phải chủ Post (tự sướng không cần noti)
    // 2. QUAN TRỌNG: Chủ Post KHÁC Chủ Comment (Nếu trùng thì đã gửi ở trên rồi,
    // không gửi nữa)
    boolean isSenderPostOwner = event.getAuthorId().equals(event.getPostOwnerId());
    boolean isPostOwnerSameAsCommentOwner = event.getPostOwnerId().equals(event.getParentCommentOwnerId());

    if (!isSenderPostOwner && !isPostOwnerSameAsCommentOwner) {
      String title = event.getSenderName() + " đã trả lời bình luận của bạn";
      UUID recipientId = event.getParentCommentOwnerId();
      publishCommentEvent(event, title, recipientId);
    }
  }

  private void publishCommentEvent(CreatedCommentEvent event, String title, UUID recipientId) {
    NotificationEventDTO dto = NotificationEventDTO.builder()
        .referenceId(event.getId())
        .type(ResourceType.COMMENT)
        .parentReferenceId(event.getParentCommentId())
        .relatedId(event.getPostId())
        .title(title)
        .content(event.getContent())
        .senderId(event.getAuthorId())
        .senderName(event.getSenderName())
        .receiverIds(Set.of(recipientId))
        .createdAt(event.getCreatedDateTime())
        .build();
    EventEnvelope eventEnvelope = EventEnvelope.from(EventType.NOTIFICATION, dto, String.valueOf(ResourceType.COMMENT));

    streamProducer.publish(eventEnvelope);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async
  public void handleReactionNotificationEvent(ReactionEvent event) {
    try {
      // Logic xác định tiêu đề
      String title = "";
      String contentSuffix = "";
      ResourceType resourceType;

      if (event.getTargetType() == TargetType.POST) {
        title = "Ai đó đã bày tỏ cảm xúc về bài viết của bạn";
        contentSuffix = " vào bài viết của bạn.";
        resourceType = ResourceType.POST;
      } else {
        title = "Ai đó đã bày tỏ cảm xúc về bình luận của bạn";
        contentSuffix = " vào bình luận của bạn.";
        resourceType = ResourceType.COMMENT;
      }

      String content = event.getSenderName() + " đã thả " + event.getType() + contentSuffix;

      // Tạo DTO notification và gửi qua NotificationHandler
      NotificationEventDTO dto = NotificationEventDTO.builder()
          .referenceId(event.getTargetId())
          .type(resourceType)
          .title(title)
          .content(content)
          .senderId(event.getSenderId())
          .senderName(event.getSenderName())
          .receiverIds(Collections.singleton(event.getReceiverId()))
          .createdAt(java.time.LocalDateTime.now())
          .build();

      log.info("Sending reaction notification for target: {}", event.getTargetId());
      notificationHandler.handleNotification(dto);

    } catch (Exception e) {
      log.error("Failed to send reaction notification", e);
    }
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleNotificationEvent(NotificationEvent event) {
    log.info("Received event: {}", event.getTitle());

    try {
      ResourceType mappedType = ResourceType.NOTIFICATION_EVENT;
      if (event.getType() == NotificationType.POST_APPROVED || event.getType() == NotificationType.POST_REJECTED) {
        mappedType = ResourceType.POST;
      } else if (event.getType() == NotificationType.DRIVE_UPLOAD
          || event.getType() == NotificationType.DOCUMENT_APPROVED
          || event.getType() == NotificationType.DOCUMENT_REJECTED) {
        mappedType = ResourceType.DOCUMENT;
      }

      UUID refId = safeUUID(event.getReferenceId());
      UUID recipientId = safeUUID(event.getRecipientId());
      UUID senderId = "SYSTEM".equalsIgnoreCase(event.getSenderId()) ? null : safeUUID(event.getSenderId());

      NotificationEventDTO dto = NotificationEventDTO.builder()
          .referenceId(refId)
          .type(mappedType)
          .title(event.getTitle())
          .content(event.getContent())
          .senderId(senderId)
          .senderName("SYSTEM".equalsIgnoreCase(event.getSenderId()) ? "Hệ thống" : "Unknown") // Fallback name
          .receiverIds(recipientId != null ? Set.of(recipientId) : Collections.emptySet())
          .createdAt(java.time.LocalDateTime.now())
          .build();

      notificationHandler.handleNotification(dto);

    } catch (Exception e) {
      log.error("Error handling NotificationEvent: {}", e.getMessage(), e);
    }
  }

  private UUID safeUUID(String uuidStr) {
    if (uuidStr == null)
      return null;
    try {
      return UUID.fromString(uuidStr);
    } catch (IllegalArgumentException e) {
      log.warn("Invalid UUID string: {}", uuidStr);
      return null;
    }
  }
}
