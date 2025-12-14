package com.graduation.project.event.dto;

import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.event.constant.NotificationStatus;
import com.graduation.project.event.entity.UserNotification;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserNotificationResponse {
  private UUID id;

  private UUID referenceId;
  private ResourceType resourceType;
  private UUID parentReferenceId;
  private UUID relatedId;

  private String title;

  private Instant deliveredAt;
  private Instant readAt;
  private NotificationStatus notificationStatus;

  public static UserNotificationResponse toUserNotificationResponse(
      UserNotification userNotification) {
    return UserNotificationResponse.builder()
        .id(userNotification.getId())
        .relatedId(userNotification.getNotificationEvent().getRelatedId())
        .resourceType(userNotification.getNotificationEvent().getType())
        .parentReferenceId(userNotification.getNotificationEvent().getParentReferenceId())
        .relatedId(userNotification.getNotificationEvent().getRelatedId())
        .title(userNotification.getNotificationEvent().getTitle())
        .deliveredAt(userNotification.getDeliveredAt())
        .readAt(userNotification.getReadAt())
        .notificationStatus(userNotification.getNotificationStatus())
        .build();
  }
}
