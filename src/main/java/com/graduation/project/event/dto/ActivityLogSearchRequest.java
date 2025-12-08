package com.graduation.project.event.dto;

import com.graduation.project.common.constant.ResourceType;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class ActivityLogSearchRequest {
  private String keyword; // Tìm trong description hoặc action
  private UUID userId; // Lọc theo người dùng cụ thể
  private String module; // Lọc theo module (ví dụ: POST, COMMENT)
  private ResourceType targetType;
  private LocalDate fromDate; // Lọc theo ngày bắt đầu
  private LocalDate toDate; // Lọc theo ngày kết thúc
}
