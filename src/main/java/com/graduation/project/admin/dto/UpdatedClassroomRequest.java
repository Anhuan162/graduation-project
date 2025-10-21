package com.graduation.project.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatedClassroomRequest {
  private String className;
  private int startedYear;
  private int endedYear;
  private String schoolYearCode;
}
