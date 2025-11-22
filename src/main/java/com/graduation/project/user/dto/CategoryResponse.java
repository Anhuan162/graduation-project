package com.graduation.project.user.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CategoryResponse {
  private UUID id;
  private String name;
  private String description;
  private String categoryType;
  private LocalDateTime createdAt;
  private String createdById;
}
