package com.graduation.project.event.entity;

import com.graduation.project.event.constant.NotificationType;
import com.graduation.project.common.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification_events")
public class NotificationEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  private User createdBy;

  @Enumerated(EnumType.STRING)
  private NotificationType type; // ANNOUNCEMENT, SYSTEM...

  private UUID relatedId; // annoucmentId, postId
}
