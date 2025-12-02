package com.graduation.project.forum.entity;

import com.graduation.project.forum.constant.ReactionType;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.common.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "reactions",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "target_id", "target_type"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reaction {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  // Map quan hệ với User (Người thả tim)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // ID của bài viết hoặc comment (Không map @ManyToOne để linh hoạt)
  @Column(name = "target_id", nullable = false)
  private UUID targetId;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false)
  private TargetType targetType;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private ReactionType type;

  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;
}
