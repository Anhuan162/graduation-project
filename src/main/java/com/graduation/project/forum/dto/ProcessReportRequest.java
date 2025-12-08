package com.graduation.project.forum.dto;

import com.graduation.project.forum.constant.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcessReportRequest {
  @NotNull(message = "Status is required")
  private ReportStatus status;

  private String adminNote; // Ghi chú của admin về quyết định này (optional)

  private boolean deleteTarget; // True = Xóa luôn bài viết/comment bị report
}
