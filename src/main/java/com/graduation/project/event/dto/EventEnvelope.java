package com.graduation.project.event.dto;

import com.graduation.project.event.constant.EventType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventEnvelope {
  private UUID eventId;
  private EventType eventType; // e.g. "NOTIFICATION", "ACTIVITY"
  private Object payload; // raw payload (map -> convert to DTO)
  private int retryCount;
  private int version;
  private String producer;
  private LocalDateTime createdAt;

  public static EventEnvelope from(EventType type, Object payload, String producer) {
    return EventEnvelope.builder()
        .eventId(UUID.randomUUID()) // Trace ID duy nhất cho sự kiện này
        .eventType(type) // QUAN TRỌNG: Định danh để Consumer biết đây là log
        .payload(payload) // Dữ liệu chính
        .retryCount(0) // Mới tạo thì retry = 0
        .version(1) // Version 1 của cấu trúc message
        .producer(producer) // Tên service tạo ra event này
        .createdAt(LocalDateTime.now())
        .build();
  }
}
