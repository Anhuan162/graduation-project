package com.graduation.project.event.listener;

import com.graduation.project.event.constant.EventType;
import com.graduation.project.event.dto.ActivityLogDTO;
import com.graduation.project.event.dto.EventEnvelope;
import com.graduation.project.event.entity.DomainEvent;
import com.graduation.project.event.producer.StreamProducer;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async; // Nếu muốn chạy background
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ActivityLogListener {

  private final StreamProducer streamProducer;

  // Annotation này giúp method tự động chạy khi có DomainEvent được publish
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async // (Tuỳ chọn) Giúp logic log không làm chậm luồng chính của user
  public void handleDomainEvent(DomainEvent event) {
    // 1. Build DTO (Đoạn code dài dòng chuyển về đây)
    ActivityLogDTO log =
        ActivityLogDTO.builder()
            .id(UUID.randomUUID()) // Tạo mới ID cho log
            .userId(event.getActorId()) // Người làm
            .action(event.getAction())
            .module(event.getModule())
            .description(event.getMessage()) // Mô tả hành động
            .targetId(event.getResourceId())
            .targetType(event.getResourceType())
            //            .metadata(convertMapToJson(event.getMetadata())) // Chuyển Map thành
            // String JSON
            .ipAddress(event.getIpAddress())
            .userAgent(event.getUserAgent())
            .createdAt(event.getLocalDateTime())
            .build();

    // 2. Đóng gói Envelope
    EventEnvelope eventEnvelope =
        EventEnvelope.from(EventType.ACTIVITY_LOG, log, event.getResourceType().name());

    // 3. Đẩy vào Redis Stream
    streamProducer.publish(eventEnvelope);
  }
}
