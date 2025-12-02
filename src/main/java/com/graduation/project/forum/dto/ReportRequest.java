package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.ReportReason;
import com.graduation.project.forum.constant.TargetType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class ReportRequest {
  @NotNull private TargetType targetType; // POST hoặc COMMENT

  @NotNull private UUID targetId; // ID của post hoặc comment

  @NotNull private ReportReason reason;

  private String description;
}
