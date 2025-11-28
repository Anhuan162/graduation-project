package com.graduation.project.event.producer;

import com.fasterxml.jackson.databind.ObjectMapper; // Import Jackson
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DlqProducer {

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper; // Inject ObjectMapper

  public void sendToDlq(
      String dlqStreamKey,
      String originalMessageId,
      Map<Object, Object> originalPayload,
      int retryCount,
      String reason) {
    try {
      // FIX: Convert Map sang JSON String chuẩn
      String payloadJson =
          (originalPayload != null) ? objectMapper.writeValueAsString(originalPayload) : "{}";

      MapRecord<String, String, Object> record =
          MapRecord.create(
              dlqStreamKey,
              Map.of(
                  "originalId", originalMessageId,
                  "payload", payloadJson, // Lưu JSON valid
                  "retryCount", String.valueOf(retryCount),
                  "failedAt", Instant.now().toString(),
                  "reason", reason));

      redisTemplate.opsForStream().add(record);
      log.warn("Moved to DLQ {} originalId={}", dlqStreamKey, originalMessageId);
    } catch (Exception e) {
      log.error("Failed to write to DLQ", e);
    }
  }
}
