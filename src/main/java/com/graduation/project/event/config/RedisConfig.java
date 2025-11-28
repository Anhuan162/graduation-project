package com.graduation.project.event.config;

import java.nio.charset.StandardCharsets;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ReadOffset;

@Log4j2
public abstract class RedisConfig {
  @Value("${redis.stream.key}")
  protected String streamKey;

  private final RedisConnectionFactory connectionFactory;

  public RedisConfig(RedisConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  protected void initGroupIfNeeded(String groupName) {
    try (RedisConnection connection = connectionFactory.getConnection()) {
      byte[] streamKeyBytes = streamKey.getBytes(StandardCharsets.UTF_8);

      // Trường hợp 1: Stream chưa tồn tại -> Tạo Stream + Group luôn (mkstream = true)
      if (!Boolean.TRUE.equals(connection.keyCommands().exists(streamKeyBytes))) {
        connection
            .streamCommands()
            .xGroupCreate(streamKeyBytes, groupName, ReadOffset.from("0"), true);
        log.info("✔ Created stream & group: {}", groupName);
        return;
      }

      // Trường hợp 2: Stream đã tồn tại -> Chỉ tạo Group
      try {
        connection
            .streamCommands()
            .xGroupCreate(streamKeyBytes, groupName, ReadOffset.from("0"), false);
        log.info("✔ Created group: {}", groupName);

      } catch (Exception e) {
        // --- SỬA TẠI ĐÂY ---
        // Kiểm tra message của wrapper HOẶC message của nguyên nhân gốc (cause)
        if (e.getMessage().contains("BUSYGROUP")
            || (e.getCause() != null && e.getCause().getMessage().contains("BUSYGROUP"))) {

          log.info("✔ Group already exists: {}", groupName);

        } else {
          // Nếu là lỗi khác (ví dụ mất kết nối, sai quyền...) thì vẫn phải ném ra
          throw e;
        }
      }
    }
  }
}
