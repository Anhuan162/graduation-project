package com.graduation.project.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectReferenceRequest {
  @NotNull(message = "subjectId không được để trống")
  private String subjectId;

  @NotNull(message = "facultyId không được để trống")
  private String facultyId;

  @NotNull(message = "semesterId không được để trống")
  private Integer semesterId;

  @NotNull(message = "cohortCode không được để trống")
  private String cohortCode;
}
