package com.graduation.project.common.dto;

import com.graduation.project.common.constant.ResourceType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class SearchFileRequest {
  private String folder;
  private ResourceType resourceType;
  private UUID resourceId;
  LocalDateTime toDate;
  LocalDateTime fromDate;
}
