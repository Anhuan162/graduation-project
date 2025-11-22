package com.graduation.project.user.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TopicResponse {
  private String id;
  private String categoryId;
  private String title;
  private String content;
  private LocalDateTime createdAt;
  private String createdBy;
}
