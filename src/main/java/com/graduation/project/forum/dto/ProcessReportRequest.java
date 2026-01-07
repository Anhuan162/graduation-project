package com.graduation.project.forum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcessReportRequest {
  @NotNull(message = "Action is required")
  private ReportAction action;

  private String note; // Lý do xử lý

  public enum ReportAction {
    DELETE_CONTENT,
    KEEP_CONTENT,
    WARN_USER
  }
}
