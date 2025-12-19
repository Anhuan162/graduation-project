package com.graduation.project.event.listener;

import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.event.constant.EventType;
import com.graduation.project.event.dto.EventEnvelope;
import com.graduation.project.event.dto.NotificationEventDTO; // DTO riêng cho thông báo
import com.graduation.project.event.producer.StreamProducer;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.forum.dto.CreatedCommentEvent;
import com.graduation.project.forum.dto.ReactionEvent;
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
    // 2. QUAN TRỌNG: Chủ Post KHÁC Chủ Comment (Nếu trùng thì đã gửi ở trên rồi, không gửi nữa)
    boolean isSenderPostOwner = event.getAuthorId().equals(event.getPostOwnerId());
    boolean isPostOwnerSameAsCommentOwner =
        event.getPostOwnerId().equals(event.getParentCommentOwnerId());

    if (!isSenderPostOwner && !isPostOwnerSameAsCommentOwner) {
      String title = event.getSenderName() + "đã trả lời bình luận của bạn";
      UUID recipientId = event.getParentCommentOwnerId();
      publishCommentEvent(event, title, recipientId);
    }
  }

  private void publishCommentEvent(CreatedCommentEvent event, String title, UUID recipientId) {
    NotificationEventDTO dto =
        NotificationEventDTO.builder()
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
    EventEnvelope eventEnvelope =
        EventEnvelope.from(EventType.NOTIFICATION, dto, String.valueOf(ResourceType.COMMENT));

    streamProducer.publish(eventEnvelope);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async
  public void handleReactionNotificationEvent(ReactionEvent event) {
    try {
      // Logic xác định tiêu đề (Có thể làm đẹp hơn bằng MessageSource/i18n sau này)
      String title = "";
      String contentSuffix = "";

      if (event.getTargetType() == TargetType.POST) {
        title = "Ai đó đã bày tỏ cảm xúc về bài viết của bạn";
        contentSuffix = " vào bài viết của bạn.";
      } else {
        title = "Ai đó đã bày tỏ cảm xúc về bình luận của bạn";
        contentSuffix = " vào bình luận của bạn.";
      }

      String content = event.getSenderName() + " đã thả " + event.getType() + contentSuffix;

      // Tạo DTO notification
      NotificationEventDTO dto =
          NotificationEventDTO.builder()
              .relatedId(event.getTargetId())
              .type(ResourceType.REACTION)
              .title(title)
              .content(content)
              .senderId(event.getSenderId())
              .senderName(event.getSenderName())
              .receiverIds(Collections.singleton(event.getReceiverId())) // <--- Dùng ID có sẵn
              .createdAt(java.time.LocalDateTime.now()) // Hoặc truyền từ event sang
              .build();

      EventEnvelope eventEnvelope =
          EventEnvelope.from(EventType.NOTIFICATION, dto, "REACTION_SERVICE");

      log.info("Sending reaction notification event for target: {}", event.getTargetId());
      streamProducer.publish(eventEnvelope);

    } catch (Exception e) {
      log.error("Failed to send reaction notification", e);
    }
  }
}
