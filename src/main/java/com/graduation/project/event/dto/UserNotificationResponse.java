package com.graduation.project.event.dto;

import com.graduation.project.common.entity.UserNotification;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserNotificationResponse {
  private UUID id;
  private UUID notificationEventId;

  private UUID userId;

  private boolean isRead;
  private Instant deliveredAt;
  private Instant readAt;

  private String notificationStatus;

  public static UserNotificationResponse toUserNotificationResponse(
      UserNotification userNotification) {
    return UserNotificationResponse.builder()
        .id(userNotification.getId())
        .notificationStatus(userNotification.getNotificationStatus().toString())
        .userId(userNotification.getUser().getId())
        .isRead(userNotification.isRead())
        .deliveredAt(userNotification.getDeliveredAt())
        .readAt(userNotification.getReadAt())
        .notificationEventId(userNotification.getNotificationEvent().getId())
        .build();
  }
}
