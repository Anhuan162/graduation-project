package com.graduation.project.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class NotificationStreamConsumer
    implements StreamListener<String, MapRecord<String, String, String>> {

  private final NotificationService notificationService;
  private final ObjectMapper redisObjectMapper;

  @Override
  public void onMessage(MapRecord<String, String, String> record) {
    try {
      // Lấy payload JSON
      String json = record.getValue().get("payload");

      // Parse JSON → DTO
      NotificationMessageDTO dto = redisObjectMapper.readValue(json, NotificationMessageDTO.class);

      log.info("Received message: {}", dto);
      notificationService.handleNotification(dto);

    } catch (Exception e) {
      log.error("Lỗi khi xử lý message từ stream: ", e);
    }
  }
}
