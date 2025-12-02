package com.graduation.project.event.entity;

import com.graduation.project.event.constant.NotificationStatus;
import com.graduation.project.common.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_notifications")
public class UserNotification {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "notification_event_id")
  private NotificationEvent notificationEvent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  private boolean isRead;
  private Instant deliveredAt;
  private Instant readAt;

  @Enumerated(EnumType.STRING)
  private NotificationStatus notificationStatus;
}
