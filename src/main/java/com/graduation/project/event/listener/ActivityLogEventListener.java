package com.graduation.project.event.listener;

import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.event.constant.EventType;
import com.graduation.project.event.dto.ActivityLogDTO;
import com.graduation.project.event.dto.EventEnvelope;
import com.graduation.project.event.producer.StreamProducer;
import com.graduation.project.forum.dto.CreatedCommentEvent;
import com.graduation.project.forum.dto.CreatedPostEvent;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityLogEventListener {

  private final StreamProducer streamProducer;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async
  public void handleCommentEvent(CreatedCommentEvent event) {
    if (Objects.nonNull(event.getParentCommentId())) {
      String description = "Bạn đã trả lời bình luận " + event.getParentCommentId();
      publishCommentEvent(event, description);

    } else {
      String description = "Bạn đã bình luận bài viết " + event.getPostId();
      publishCommentEvent(event, description);
    }
  }

  private void publishCommentEvent(CreatedCommentEvent event, String description) {
    ActivityLogDTO activityLogDTO =
        ActivityLogDTO.builder()
            .userId(event.getAuthorId())
            .action("CREATE_COMMENT")
            .module("FORUM")
            .targetType(ResourceType.COMMENT)
            .targetId(event.getPostId())
            .description(description)
            .ipAddress("")
            .createdAt(LocalDateTime.now())
            .build();
    EventEnvelope eventEnvelope =
        EventEnvelope.from(
            EventType.ACTIVITY_LOG, activityLogDTO, String.valueOf(ResourceType.COMMENT));

    streamProducer.publish(eventEnvelope);
  }

  @TransactionalEventListener
  @Async
  public void handleCreatedPostEvent(CreatedPostEvent event) {
    ActivityLogDTO activityLogDTO =
        ActivityLogDTO.builder()
            .userId(event.getAuthorId())
            .action("CREATE_POST")
            .module("FORUM")
            .targetType(ResourceType.POST)
            .targetId(event.getPostId())
            .description("Bạn đã tạo bài đăng " + event.getPostId())
            .ipAddress("")
            .createdAt(LocalDateTime.now())
            .build();

    EventEnvelope eventEnvelope =
        EventEnvelope.from(
            EventType.ACTIVITY_LOG, activityLogDTO, String.valueOf(ResourceType.POST));

    streamProducer.publish(eventEnvelope);
  }
}
