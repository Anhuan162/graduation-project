package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcessReportRequest {
  @NotNull(message = "Status is required")
  private ReportStatus status;

  private String adminNote;

  private boolean deleteTarget;
}
