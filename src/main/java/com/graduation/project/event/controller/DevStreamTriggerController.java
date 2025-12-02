package com.graduation.project.event.controller;

import com.graduation.project.event.constant.EventType;
import com.graduation.project.event.dto.EventEnvelope;
import com.graduation.project.event.producer.StreamProducer;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Chỉ kích hoạt khi profile là dev hoặc test
@RestController
@RequestMapping("/api/v1/dev/trigger")
@RequiredArgsConstructor
public class DevStreamTriggerController {

  private final StreamProducer producer;

  @PostMapping("/notification")
  public ResponseEntity<String> manualTriggerNotification(@RequestBody Object payload) {
    // Giả lập một Event Envelope
    EventEnvelope event =
        EventEnvelope.builder()
            .eventId(UUID.randomUUID())
            .eventType(EventType.NOTIFICATION)
            .payload(payload) // Payload từ Body Postman
            .producer("MANUAL_TRIGGER_DEV")
            .createdAt(LocalDateTime.now())
            .version(1)
            .retryCount(0)
            .build();

    producer.publish(event);
    return ResponseEntity.ok("✅ Đã bắn event NOTIFICATION vào Redis Stream");
  }

  @PostMapping("/activity")
  public ResponseEntity<String> manualTriggerActivity(@RequestBody Object payload) {
    EventEnvelope event =
        EventEnvelope.builder()
            .eventId(UUID.randomUUID())
            .eventType(EventType.ACTIVITY_LOG) // Type Activity
            .payload(payload)
            .producer("MANUAL_TRIGGER_DEV")
            .createdAt(LocalDateTime.now())
            .build();

    producer.publish(event);
    return ResponseEntity.ok("✅ Đã bắn event ACTIVITY vào Redis Stream");
  }
}
