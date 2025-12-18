package com.graduation.project.event.listener;

import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.event.constant.EventType;
import com.graduation.project.event.dto.ActivityLogDTO;
import com.graduation.project.event.dto.EventEnvelope;
import com.graduation.project.event.dto.NotificationEventDTO; // DTO riêng cho thông báo
import com.graduation.project.event.producer.StreamProducer;
import com.graduation.project.forum.dto.CreatedCommentEvent;
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
  public void handleNotificationEvent(NotificationEventDTO event) {

    EventEnvelope eventEnvelope =
        EventEnvelope.from(EventType.NOTIFICATION, event, String.valueOf(event.getType()));

    streamProducer.publish(eventEnvelope);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async
  public void handleDomainEvent(ActivityLogDTO event) {

    EventEnvelope eventEnvelope =
        EventEnvelope.from(EventType.ACTIVITY_LOG, event, String.valueOf(event.getTargetType()));

    streamProducer.publish(eventEnvelope);
  }

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
}
