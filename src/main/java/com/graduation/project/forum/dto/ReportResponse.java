package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.ReportReason;
import com.graduation.project.forum.constant.ReportStatus;
import com.graduation.project.forum.constant.TargetType;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {
  private UUID id;
  private UUID reporterId;
  private ReportReason reason;

  private String description;

  private ReportStatus status;
  private TargetType targetType;
  private UUID postId;
  private UUID commentId;

  private String ipAddress;
  private Instant createdAt;
  private String targetPreview;
  private UUID processedById;
  private Instant processedAt;
}
