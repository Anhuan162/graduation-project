package com.graduation.project.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graduation.project.event.dto.NotificationEventDTO;
import com.graduation.project.event.service.IdempotencyService;
import com.graduation.project.event.service.NotificationHandler;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class NotificationStreamConsumer
    implements StreamListener<String, MapRecord<String, Object, Object>> {

  private final NotificationHandler notificationHandler;
  private final ObjectMapper redisObjectMapper;
  private final RedisTemplate<String, Object> redisTemplate;
  private final IdempotencyService idempotencyService;

  @Value("${redis.stream.key}")
  private String streamKey;

  @Value("${redis.stream.notification-group}")
  private String notificationGroupName;

  @Value("${redis.retry.processed-ttl-seconds:86400}")
  private long processedTtlSeconds;

  @Override
  public void onMessage(MapRecord<String, Object, Object> record) {
    String messageId = record.getId().getValue();
    try {
      Map<Object, Object> value = record.getValue();

      Object typeObj = value.get("type");
      String type = (typeObj != null) ? typeObj.toString() : "";
      Object eventIdObj = value.get("eventId");
      String eventId = (eventIdObj != null) ? eventIdObj.toString() : "";

      if (!"NOTIFICATION".equals(type)) {
        redisTemplate.opsForStream().acknowledge(streamKey, notificationGroupName, record.getId());
        log.debug("Ignored & Acked non-notification type: {}", type);
        return;
      }
      Object payloadObj = value.get("payload");
      String payloadJson =
          (payloadObj instanceof String)
              ? (String) payloadObj
              : redisObjectMapper.writeValueAsString(payloadObj);

      NotificationEventDTO dto =
          redisObjectMapper.readValue(payloadJson, NotificationEventDTO.class);

      // Idempotency
      if (!idempotencyService.tryLock(eventId, processedTtlSeconds)) {
        log.info("Already processed eventId={}, acking {}", eventId, messageId);
        redisTemplate.opsForStream().acknowledge(streamKey, notificationGroupName, record.getId());
        return;
      }

      notificationHandler.handleNotification(dto);

      idempotencyService.markProcessed(eventId, processedTtlSeconds);
      redisTemplate.opsForStream().acknowledge(streamKey, notificationGroupName, record.getId());

      log.info("Processed notification {} messageId={}", eventId, messageId);

    } catch (Exception e) {
      log.error("Failed to process message {}: {}", messageId, e.getMessage());
    }
  }
}
