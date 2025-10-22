package com.graduation.project.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatedClassroomRequest {
  private String className;
  private String classCode;
  private int startedYear;
  private int endedYear;
  private String schoolYearCode;
}
