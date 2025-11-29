package com.graduation.project.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graduation.project.event.dto.ActivityLogDTO;
import com.graduation.project.event.service.ActivityLogHandler;
import com.graduation.project.event.service.IdempotencyService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityStreamConsumer
    implements StreamListener<String, MapRecord<String, Object, Object>> {

  private final ObjectMapper redisObjectMapper;
  private final IdempotencyService idempotencyService;
  private final RedisTemplate<String, Object> redisTemplate;
  private final ActivityLogHandler activityLogHandler;

  @Value("${redis.stream.activity-group}")
  private String activityGroupName;

  @Value("${redis.stream.key}")
  private String streamKey;

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

      if (!"ACTIVITY_LOG".equals(type)) {
        redisTemplate.opsForStream().acknowledge(streamKey, activityGroupName, record.getId());
        log.debug("Ignored non-activity type: {} msgId={}", type, messageId);
        return;
      }

      Object payloadObj = value.get("payload");
      String payloadJson =
          (payloadObj instanceof String)
              ? (String) payloadObj
              : redisObjectMapper.writeValueAsString(payloadObj);

      ActivityLogDTO dto = redisObjectMapper.readValue(payloadJson, ActivityLogDTO.class);

      if (!idempotencyService.tryLock(eventId, processedTtlSeconds)) {
        log.info("Already processed activity eventId={}, messageId={}", eventId, messageId);
        redisTemplate.opsForStream().acknowledge(streamKey, activityGroupName, record.getId());
        return;
      }

      activityLogHandler.handleActivityLog(dto);

      // Mark processed & Ack
      idempotencyService.markProcessed(eventId, processedTtlSeconds);
      redisTemplate.opsForStream().acknowledge(streamKey, activityGroupName, record.getId());

      log.info("Processed activity eventId={}, messageId={}", eventId, messageId);

    } catch (Exception e) {
      log.error("ActivityStreamConsumer failed messageId={} err={}", messageId, e.getMessage(), e);
      // Message sẽ ở lại Pending để Scanner retry sau (đúng logic cho trường hợp lỗi thật)
    }
  }
}
