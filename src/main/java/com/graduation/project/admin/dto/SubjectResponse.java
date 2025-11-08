package com.graduation.project.admin.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponse {
  private UUID id;
  private String subjectName;
  private String subjectCode;
  private int credit;
  private String description;
  private LocalDateTime createdDate;
  private LocalDateTime lastModifiedDate;
}
