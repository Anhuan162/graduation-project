//package com.graduation.project.event.scheduler;
//
//import com.graduation.project.event.consumer.NotificationStreamConsumer;
//import java.util.List;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.domain.Range;
//import org.springframework.data.redis.connection.stream.*;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.stream.StreamListener;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Log4j2
//public class StreamPendingMessageScheduler {
//
//  private final RedisTemplate<String, Object> redisTemplate;
//
//  // Inject cả 2 consumer
//  private final NotificationStreamConsumer consumer;
//
//  @Value("${redis.stream.key}")
//  private String streamKey;
//
//  @Value("${redis.stream.notification-group}")
//  private String notificationGroup;
//
//  @Value("${redis.stream.activity-group}")
//  private String activityGroup;
//
//  @Scheduled(fixedDelay = 60000) // Chạy mỗi phút
//  public void processPendingMessages() {
//    // 1. Quét lỗi cho Notification
//    processPendingForGroup(notificationGroup, "notification-consumer-1", consumer);
//
//    // 2. Quét lỗi cho Activity
//    processPendingForGroup(activityGroup, "activity-consumer-1", consumer);
//  }
//
//  /** Hàm generic xử lý pending message cho bất kỳ group nào */
//  private void processPendingForGroup(
//      String groupName,
//      String consumerName,
//      StreamListener<String, MapRecord<String, Object, Object>> consumer) {
//    try {
//      // 1. Kiểm tra xem có message nào đang pending không
//      PendingMessages pendingMessages =
//          redisTemplate.opsForStream().pending(streamKey, groupName, Range.unbounded(), 10);
//
//      if (pendingMessages.isEmpty()) return;
//
//      log.info("Found {} pending messages in group {}", pendingMessages.size(), groupName);
//
//      // 2. Đọc lại các message pending đó
//      // SỬA LỖI TẠI ĐÂY: Dùng StreamOffset.create(key, offset)
//      List<MapRecord<String, Object, Object>> messages =
//          redisTemplate
//              .opsForStream()
//              .read(
//                  Consumer.from(groupName, consumerName),
//                  StreamReadOptions.empty().count(10),
//                  StreamOffset.create(
//                      streamKey, ReadOffset.from("0")) // <--- FIX: Phải bọc trong StreamOffset
//                  );
//
//      if (messages != null) {
//        for (MapRecord<String, Object, Object> record : messages) {
//          log.info("Reprocessing pending message in {}: {}", groupName, record.getId());
//
//          // Tái sử dụng logic onMessage của consumer tương ứng
//          consumer.onMessage(record);
//        }
//      }
//    } catch (Exception e) {
//      log.error("Error processing pending messages for group {}", groupName, e);
//    }
//  }
//}
