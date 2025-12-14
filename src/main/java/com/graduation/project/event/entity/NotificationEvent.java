package com.graduation.project.event.entity;

import com.graduation.project.common.constant.ResourceType;
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

  private UUID referenceId;

  @Enumerated(EnumType.STRING)
  private ResourceType type;

  private UUID parentReferenceId;

  private UUID relatedId;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  private User createdBy;
}
