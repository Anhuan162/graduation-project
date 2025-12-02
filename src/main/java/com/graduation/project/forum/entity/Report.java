package com.graduation.project.forum.entity;

import com.graduation.project.forum.constant.ReportReason;
import com.graduation.project.forum.constant.ReportStatus;
import com.graduation.project.forum.constant.TargetType;
import com.graduation.project.common.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(
    name = "reports",
    indexes = {
      @Index(name = "idx_report_status", columnList = "status"),
      @Index(name = "idx_report_created_at", columnList = "createdAt")
    })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reporter_id", nullable = false)
  private User reporter;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReportReason reason;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private ReportStatus status = ReportStatus.PENDING;

  // --- PHẦN QUAN TRỌNG: POLYMORPHISM ---

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TargetType targetType;

  // Liên kết Post (Nullable)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;

  // Liên kết Comment (Nullable)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "comment_id")
  private Comment comment;

  // --- END POLYMORPHISM ---

  private String ipAddress;
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    if (this.status == null) this.status = ReportStatus.PENDING;
  }

  // Validate logic: 1 Report chỉ được thuộc về Post HOẶC Comment, không cả hai
  @AssertTrue(message = "Report must target either a Post or a Comment")
  public boolean isValidTarget() {
    if (targetType == TargetType.POST) {
      return post != null && comment == null;
    } else if (targetType == TargetType.COMMENT) {
      return comment != null && post == null;
    }
    return false;
  }
}
