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
  private String content;
  private UUID senderId;
  private String senderName;

  private Instant deliveredAt;
  private Instant readAt;
  private NotificationStatus notificationStatus;

  public static UserNotificationResponse toUserNotificationResponse(
      UserNotification userNotification) {
    return UserNotificationResponse.builder()
        .id(userNotification.getId())
        .referenceId(userNotification.getNotificationEvent().getReferenceId())
        .resourceType(userNotification.getNotificationEvent().getType())
        .parentReferenceId(userNotification.getNotificationEvent().getParentReferenceId())
        .relatedId(userNotification.getNotificationEvent().getRelatedId())
        .title(userNotification.getNotificationEvent().getTitle())
        .content(userNotification.getNotificationEvent().getContent())
        .senderId(userNotification.getNotificationEvent().getCreatedBy() != null
            ? userNotification.getNotificationEvent().getCreatedBy().getId()
            : null)
        .senderName(userNotification.getNotificationEvent().getCreatedBy() != null
            ? userNotification.getNotificationEvent().getCreatedBy().getFullName()
            : "System")
        .deliveredAt(userNotification.getDeliveredAt())
        .readAt(userNotification.getReadAt())
        .notificationStatus(userNotification.getNotificationStatus())
        .build();
  }
}
