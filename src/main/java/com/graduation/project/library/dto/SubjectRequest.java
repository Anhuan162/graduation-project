package com.graduation.project.library.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectRequest {
  @NotNull private String subjectName;
  @NotNull private String subjectCode;
  @NotNull private int credit;
  private String description;
}
