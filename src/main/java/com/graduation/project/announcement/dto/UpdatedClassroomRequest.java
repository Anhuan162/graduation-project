package com.graduation.project.announcement.dto;

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
