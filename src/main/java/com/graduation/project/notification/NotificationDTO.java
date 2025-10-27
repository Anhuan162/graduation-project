package com.graduation.project.notification;

import com.graduation.project.common.entity.NotificationEvent;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
  private UUID id;
  private String title;
  private String content;
  private LocalDateTime createdAt;
  private String relatedEntityId;
  private String notificationType;

  public static NotificationDTO fromEntity(NotificationEvent n) {
    return NotificationDTO.builder()
        .id(n.getId())
        .title(n.getTitle())
        .content(n.getContent())
        .createdAt(n.getCreatedAt())
        .relatedEntityId(String.valueOf(n.getRelatedId()))
        .notificationType(String.valueOf(n.getType()))
        .build();
  }
}
