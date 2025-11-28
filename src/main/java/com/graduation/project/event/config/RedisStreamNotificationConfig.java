package com.graduation.project.event.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graduation.project.event.consumer.NotificationStreamConsumer;
import java.time.Duration;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.data.redis.stream.Subscription;

@Log4j2
@Configuration
public class RedisStreamNotificationConfig extends RedisConfig {

  @Value("${redis.stream.notification-group}")
  private String groupName;

  public RedisStreamNotificationConfig(RedisConnectionFactory factory) {
    super(factory);
  }

  @Bean
  // 🔥 UPDATE: Đổi Generic thứ 2 từ String -> Object
  public StreamMessageListenerContainer<String, MapRecord<String, Object, Object>>
      notificationListenerContainer(
          RedisConnectionFactory factory,
          NotificationStreamConsumer consumer,
          ObjectMapper redisObjectMapper) {

    initGroupIfNeeded(groupName);

    // 1. Build Options
    // 🔥 UPDATE: Đổi Generic thứ 2 từ String -> Object để khớp với cái "Found" của lỗi
    StreamMessageListenerContainerOptions<String, MapRecord<String, Object, Object>> options =
        StreamMessageListenerContainerOptions.builder()
            .pollTimeout(Duration.ofSeconds(1))
            .hashKeySerializer(new StringRedisSerializer())
            .hashValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper))
            .build();

    // 2. Create Container
    // 🔥 UPDATE: Đổi Generic thứ 2 từ String -> Object
    StreamMessageListenerContainer<String, MapRecord<String, Object, Object>> container =
        StreamMessageListenerContainer.create(factory, options);

    // 3. Register Consumer
    Subscription subscription =
        container.register(
            StreamMessageListenerContainer.StreamReadRequest.builder(
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed()))
                .consumer(Consumer.from(groupName, "notification-consumer-1"))
                .autoAcknowledge(false)
                .build(),
            consumer);

    log.info("🚀 Notification consumer registered");
    container.start();

    return container;
  }
}
