package com.graduation.project.event.scheduler;

import com.graduation.project.event.config.RedisStreamProperties;
import com.graduation.project.event.consumer.ActivityStreamConsumer;
import com.graduation.project.event.consumer.NotificationStreamConsumer;
import com.graduation.project.event.producer.DlqProducer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PendingEntryScanner {

  private final RedisTemplate<String, Object> redisTemplate;
  private final RedisStreamProperties properties;
  private final DlqProducer dlqProducer;

  // 1. Inject các Consumer để gọi xử lý lại (Retry)
  private final NotificationStreamConsumer notificationConsumer;
  private final ActivityStreamConsumer activityConsumer;

  @Scheduled(fixedDelayString = "#{@redisStreamProperties.retry.pendingScanIntervalMs}")
  public void scanPending() {
    String streamKey = properties.getStream().getKey();

    // 2. Truyền Consumer tương ứng vào hàm xử lý
    checkPendingForGroup(
        properties.getStream().getNotificationGroup(), streamKey, notificationConsumer);
    checkPendingForGroup(properties.getStream().getActivityGroup(), streamKey, activityConsumer);
  }

  /** Check pending cho một Group cụ thể và dùng Consumer tương ứng để xử lý */
  private void checkPendingForGroup(
      String groupName,
      String streamKey,
      StreamListener<String, MapRecord<String, Object, Object>> consumer) {

    try {
      PendingMessages pendingMessages =
          redisTemplate.opsForStream().pending(streamKey, groupName, Range.unbounded(), 100);

      if (pendingMessages.isEmpty()) return;

      for (PendingMessage pm : pendingMessages) {
        long deliveryCount = pm.getTotalDeliveryCount();
        long idleTime = pm.getElapsedTimeSinceLastDelivery().toMillis();
        String messageId = pm.getIdAsString();

        // Debug log (có thể tắt bớt khi chạy prod)
        log.debug(
            "Scanning id={} group={} count={} idle={}",
            messageId,
            groupName,
            deliveryCount,
            idleTime);

        // Case 1: Quá số lần thử -> Đẩy ra DLQ
        if (deliveryCount > properties.getRetry().getMaxAttempts()) {
          log.warn(
              "Message {} in group {} exceeded max retries ({})",
              messageId,
              groupName,
              deliveryCount);
          moveToDlqAndAck(streamKey, groupName, pm);
          continue;
        }

        // Case 2: Chưa quá giới hạn nhưng bị "treo" (idle) quá lâu -> Retry
        long backoff = properties.getRetry().getBaseBackoffMs() * (1L << (deliveryCount - 1));
        if (idleTime >= backoff) {
          log.info(
              "Retrying message {} in group {} (Attempt #{})", messageId, groupName, deliveryCount);
          attemptClaimAndProcess(streamKey, groupName, pm, consumer);
        }
      }
    } catch (Exception e) {
      log.error("PendingEntryScanner error group {}: {}", groupName, e.getMessage());
    }
  }

  private void attemptClaimAndProcess(
      String streamKey,
      String groupName,
      PendingMessage pm,
      StreamListener<String, MapRecord<String, Object, Object>> consumer) {

    String messageId = pm.getIdAsString();
    try {
      // Đặt tên consumer tạm thời để claim về
      String scannerConsumerName = "scanner-" + UUID.randomUUID().toString().substring(0, 8);

      RedisStreamCommands.XClaimOptions options =
          RedisStreamCommands.XClaimOptions.minIdle(Duration.ZERO).ids(pm.getId());

      // Claim message về scanner
      List<MapRecord<String, Object, Object>> claimedRecords =
          redisTemplate.opsForStream().claim(streamKey, groupName, scannerConsumerName, options);

      if (claimedRecords == null || claimedRecords.isEmpty()) {
        // Message đã bị ai đó xử lý mất rồi -> bỏ qua
        return;
      }

      MapRecord<String, Object, Object> record = claimedRecords.get(0);

      // --- SỬA LỖI QUAN TRỌNG: PROCESS IN-PLACE ---
      // Không XADD (Re-publish) nữa.
      // Gọi trực tiếp Consumer để xử lý logic nghiệp vụ.
      // Consumer của bạn đã có logic: try { xử lý } catch { log } và ACK khi thành công.

      consumer.onMessage(record);

      // Lưu ý: Vì Consumer của bạn có lệnh redisTemplate.acknowledge() bên trong nó,
      // nên Scanner không cần gọi ACK ở đây nữa.
      // Nếu Consumer ném Exception, message vẫn ở trạng thái Pending (để lần sau retry tiếp -> tăng
      // deliveryCount).

    } catch (Exception e) {
      log.error("attemptClaimAndProcess failed id={}", messageId, e);
    }
  }

  private void moveToDlqAndAck(String streamKey, String groupName, PendingMessage pm) {
    String messageId = pm.getIdAsString();
    try {
      List<MapRecord<String, Object, Object>> records =
          redisTemplate.opsForStream().range(streamKey, Range.closed(messageId, messageId));

      if (records != null && !records.isEmpty()) {
        MapRecord<String, Object, Object> record = records.get(0);
        Map<Object, Object> payload = record.getValue();
        String dlqKey = streamKey + properties.getStream().getDlqSuffix();

        // Gửi sang DLQ
        dlqProducer.sendToDlq(
            dlqKey, messageId, payload, (int) pm.getTotalDeliveryCount(), "max_retries_exceeded");
      }

      // Xóa khỏi hàng đợi chính
      redisTemplate.opsForStream().acknowledge(streamKey, groupName, messageId);
      log.warn("Moved to DLQ: {}", messageId);

    } catch (Exception e) {
      log.error("Failed DLQ move: {}", e.getMessage());
    }
  }
}
