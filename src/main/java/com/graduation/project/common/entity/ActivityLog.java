package com.graduation.project.common.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "activity_logs")
public class ActivityLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  private String action; // CREATE_ANNOUNCEMENT

  private String module; // ANNOUNCEMENT

  private String description; // "Admin created announcement: Meeting tomorrow"

  private UUID targetId; // 12345

  @Enumerated(EnumType.STRING)
  private ResourceType targetType; // ANNOUNCEMENT

  @Column(columnDefinition = "jsonb")
  private String metadata; // JSON string

  private String ipAddress;
  private String userAgent;

  private LocalDateTime createdAt = LocalDateTime.now();
}
