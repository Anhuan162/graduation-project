package com.graduation.project.notification;

import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class NotificationStreamProducer {

  private final RedisTemplate<String, Object> redisTemplate;
  private final String streamKey;

  public NotificationStreamProducer(
      RedisTemplate<String, Object> redisTemplate, @Value("${redis.stream.key}") String streamKey) {
    this.redisTemplate = redisTemplate;
    this.streamKey = streamKey;
  }

  //    public void publish(NotificationMessageDTO dto) {
  //        try {
  //            // Serialize toàn bộ DTO thành JSON String
  //            String json = redisObjectMapper.writeValueAsString(dto);
  //
  //            // Đưa vào field "payload" duy nhất
  //            MapRecord<String, String, String> record =
  //                    MapRecord.create(streamKey, Map.of("payload", json));
  //
  //            redisTemplate.opsForStream().add(record);
  //            log.info("Published message: {}", dto.getTitle());
  //        } catch (Exception e) {
  //            log.error("Lỗi khi publish tin nhắn: ", e);
  //        }
  //    }

  public void publish(NotificationMessageDTO dto) {
    try {
      // Không cần tự serialize
      MapRecord<String, String, Object> record =
          MapRecord.create(streamKey, Map.of("payload", dto));

      redisTemplate.opsForStream().add(record);
      log.info("Published message: {}", dto.getTitle());
    } catch (Exception e) {
      log.error("Lỗi khi publish tin nhắn: ", e);
    }
  }
}
