package com.graduation.project.event.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graduation.project.event.dto.EventEnvelope;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class StreamProducer {

  private final RedisTemplate<String, Object> redisTemplate;
  private final String streamKey;
  private final ObjectMapper objectMapper;
  private final Integer maxLen;

  public StreamProducer(
      RedisTemplate<String, Object> redisTemplate,
      @Value("${redis.stream.key}") String streamKey,
      @Value("${redis.stream.max-len:1000}") Integer maxLen,
      ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.streamKey = streamKey;
    this.objectMapper = objectMapper;
    this.maxLen = maxLen;
  }

  public void publish(EventEnvelope eventEnvelope) {
    try {
      String payloadJson = objectMapper.writeValueAsString(eventEnvelope.getPayload());

      MapRecord<String, String, Object> record =
          MapRecord.create(
              streamKey,
              Map.of(
                  "eventId", eventEnvelope.getEventId().toString(),
                  "type", eventEnvelope.getEventType(),
                  "payload", payloadJson, // Lưu String JSON
                  "createdAt", eventEnvelope.getCreatedAt().toString()));

      redisTemplate.opsForStream().add(record);

      if (maxLen != null && maxLen > 0) {
        redisTemplate.opsForStream().trim(streamKey, maxLen);
      }
      log.info("Published event type: {}", eventEnvelope.getEventType());

    } catch (Exception e) {
      log.error("Error publish event: ", e);
    }
  }
}
