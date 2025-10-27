package com.graduation.project.notification;

// Thêm các import này
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer; // <-- Thêm import
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;

@Configuration
@Log4j2
public class RedisStreamConfig {

  @Value("${redis.stream.key}")
  private String streamKey;

  @Value("${redis.stream.group}")
  private String groupName;

  private final RedisConnectionFactory connectionFactory;

  public RedisStreamConfig(RedisConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  // 1. Đổi tên bean này thành "redisObjectMapper"
  @Bean
  public ObjectMapper redisObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }

  // 2. Tạo bean Serializer VÀ truyền ObjectMapper vào
  @Bean
  public RedisSerializer<Object> jackson2JsonRedisSerializer(ObjectMapper redisObjectMapper) {
    return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
  }

  // 3. Sửa `redisTemplate` để inject cả 2
  @Bean
  @Primary // <-- THÊM ANNOTATION NÀY
  public RedisTemplate<String, Object> redisTemplate(
      RedisConnectionFactory factory, RedisSerializer<Object> jacksonSerializer) {
    // ... (code y hệt cũ, không cần sửa)
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(jacksonSerializer);
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(jacksonSerializer);
    template.afterPropertiesSet();
    return template;
  }

  // 2. Hàm initStream (Giữ nguyên, đã đúng)
  public void initStream() {
    // ... (Không thay đổi gì ở đây)
    try (RedisConnection connection = connectionFactory.getConnection()) {
      byte[] streamKeyBytes = streamKey.getBytes(StandardCharsets.UTF_8);

      if (Boolean.FALSE.equals(connection.keyCommands().exists(streamKeyBytes))) {
        connection
            .streamCommands()
            .xGroupCreate(streamKeyBytes, groupName, ReadOffset.from("0"), true);
        log.info("✅ Đã tạo Stream và Consumer Group: {}", groupName);
      } else {
        try {
          connection
              .streamCommands()
              .xGroupCreate(streamKeyBytes, groupName, ReadOffset.from("0"), false);
          log.info("✅ Đã tạo Consumer Group mới cho Stream: {}", groupName);
        } catch (Exception e) {
          String errorMsg = e.getMessage();
          String causeMsg = (e.getCause() != null) ? e.getCause().getMessage() : null;

          if ((errorMsg != null && errorMsg.contains("BUSYGROUP"))
              || (causeMsg != null && causeMsg.contains("BUSYGROUP"))) {
            log.warn("⚠️ Consumer Group đã tồn tại: {}", groupName);
          } else {
            throw new RuntimeException("Không thể tạo consumer group.", e);
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Không thể kết nối tới Redis để khởi tạo Stream.", e);
    }
  }

  //  @Bean
  //  public StreamMessageListenerContainer<String, MapRecord<String, String, String>>
  //      listenerContainer(
  //          RedisConnectionFactory factory,
  //          NotificationStreamConsumer consumer,
  //          RedisSerializer<Object> jacksonSerializer) {
  //
  //    // ... (code y hệt cũ, đã đúng)
  //    log.info("Đang khởi tạo Redis Stream Listener...");
  //    this.initStream();
  //
  //    var options =
  //        StreamMessageListenerContainerOptions.builder()
  //            .pollTimeout(Duration.ofSeconds(1))
  //            // *** SỬA 2: ĐỔI targetType TỪ DTO.class SANG Map.class ***
  //            .targetType(Map.class)
  //            .hashKeySerializer(new StringRedisSerializer())
  //            .hashValueSerializer(jacksonSerializer)
  //            .build();
  //
  //    StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
  //        (StreamMessageListenerContainer<String, MapRecord<String, String, String>>)
  //            (StreamMessageListenerContainer<?, ?>)
  //                StreamMessageListenerContainer.create(factory, options);
  //    container.receiveAutoAck(
  //        Consumer.from(groupName, "consumer-1"),
  //        StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
  //        consumer);
  //
  //    log.info("Consumer đã đăng ký: {}", consumer.getClass().getName());
  //    container.start();
  //    log.info("✅ Redis Stream Listener Container đã khởi động.");
  //    return container;
  //  }

  @Bean
  public StreamMessageListenerContainer<String, MapRecord<String, String, String>>
      listenerContainer(
          RedisConnectionFactory factory,
          NotificationStreamConsumer consumer,
          ObjectMapper redisObjectMapper) {

    log.info("Đang khởi tạo Redis Stream Listener...");
    this.initStream();

    //        var options = StreamMessageListenerContainerOptions.builder()
    //                .pollTimeout(Duration.ofSeconds(1))
    //                // Dùng String cho key/value → không cần JSON serializer ở đây
    //                .hashKeySerializer(new StringRedisSerializer())
    //                .hashValueSerializer(new StringRedisSerializer())
    ////                .targetType(Map.class)
    //                .build();

    var options =
        StreamMessageListenerContainerOptions.builder()
            .pollTimeout(Duration.ofSeconds(1))
            .hashKeySerializer(new StringRedisSerializer())
            .hashValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper))
            .targetType(Map.class)
            .build();

    StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
        StreamMessageListenerContainer.create(factory);

    container.receiveAutoAck(
        Consumer.from(groupName, "consumer-1"),
        StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
        consumer);

    log.info("Consumer đã đăng ký: {}", consumer.getClass().getName());
    container.start();
    log.info("✅ Redis Stream Listener Container đã khởi động.");
    return container;
  }
}
