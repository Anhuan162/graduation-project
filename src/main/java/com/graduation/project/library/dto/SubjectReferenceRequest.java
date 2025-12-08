package com.graduation.project.library.dto;

import com.graduation.project.cpa.constant.CohortCode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectReferenceRequest {
  @NotNull(message = "subjectId không được để trống")
  private UUID subjectId;

  @NotNull(message = "facultyId không được để trống")
  private UUID facultyId;

  @NotNull(message = "semesterId không được để trống")
  private Integer semesterId;

  @NotNull(message = "cohortCode không được để trống")
  private CohortCode cohortCode;
}
