package com.graduation.project.forum.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CategoryResponse {
  private UUID id;
  private String name;
  private String description;
  private String categoryType;
  private LocalDateTime createdAt;
  private String createdById;
}
