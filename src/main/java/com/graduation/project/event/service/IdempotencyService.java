package com.graduation.project.event.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdempotencyService {
  private final RedisTemplate<String, Object> redisTemplate;

  public boolean isProcessed(String eventId) {
    String key = processedKey(eventId);
    return redisTemplate.hasKey(key);
  }

  public boolean tryLock(String eventId, long ttlSeconds) {
    String key = processedKey(eventId);
    // setIfAbsent trả về true nếu key CHƯA tồn tại (đặt khóa thành công)
    // trả về false nếu key ĐÃ tồn tại (đã có người khác xử lý)
    Boolean success =
        redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(ttlSeconds));
    return Boolean.TRUE.equals(success);
  }

  public void markProcessed(String eventId, long ttlSeconds) {
    String key = processedKey(eventId);
    // Use opsForValue().setIfAbsent with TTL to ensure only first write succeeds
    redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(ttlSeconds));
  }

  private String processedKey(String eventId) {
    return "processed:" + eventId;
  }
}
