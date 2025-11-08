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
public class SemesterRequest {
  @NotNull private Integer id;

  @NotNull private String semesterType;

  @NotNull private int schoolYear;
}
